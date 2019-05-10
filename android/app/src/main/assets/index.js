const localVideo = document.querySelector('#localVideo');
showJoinGroup();

let database = firebase.database();

let groupID; // ID of joined group
const ourID = Math.floor(Math.random() *90000) + 10000;// Our user ID, randomly generated for tests

let offerRef; // Incoming offer handler, is a firebase listen object
let answerRef; // Incoming answer handler, is a firebase listen object
let closeRef; // Incoming Closing connection handler, is a firebase listen object
let iceRef; // Incoming IceCandidates handler, is a firebase listen object

let connections = {};
let ICELists = {};

// TODO: replace stream with track
// init webcam
let webcamStream;
navigator.mediaDevices.getUserMedia({
      audio: true,
      video: false
}).then( (stream) => { // I have no idea how to use tracks, so I use streams even though they're depreciated
        console.log(stream)
        webcamStream = stream;
        localVideo.srcObject = stream;
}).catch(function(e) {
        alert("Oh no!\n" + e)
});

async function joinGroup() {
    groupID = document.getElementById('grp').value;

    //start listening to offers, answers, and closes
    offerRef = database.ref('/groups/' + groupID + '/joined/' + ourID + '/offers');
    offerRef.on('child_added', onReceiveOffer);
    answerRef = database.ref('/groups/' + groupID + '/joined/' + ourID + '/answers');
    answerRef.on('child_added', onReceiveAnswer);
    closeRef = database.ref('/groups/' + groupID + '/joined/' + ourID + '/closing');
    closeRef.on('child_added', onClose);
    iceRef = database.ref('/groups/' + groupID + '/joined/' + ourID + '/ice');
    iceRef.on('child_added', onReceiveICE);

    //add ourselves to database
    await database.ref('/groups/' + groupID + '/joined/' + ourID).set("");
    console.log("Joined group as "+ ourID + ".");

    // Make connections to users already in group
    database.ref('/groups/'+ groupID + '/joined')
        .once('value', (snapshot) => {
            snapshot.forEach((childSnapshot) => {
                console.log(childSnapshot); // Having this line here solves race conditions for some reason
                makeOffer(childSnapshot);
            })
        });

    showLeaveGroup();
}

async function leaveGroup() {
    await database.ref('/groups/' + groupID + '/joined/' + ourID).set(null);

    //unsubscribe from listeners
    offerRef.off();
    answerRef.off();
    closeRef.off();
    iceRef.off();

    //close rtc connections
    Object.keys(connections).forEach((uid) => {
        closeVideoStream(uid);
        if(connections[uid].signalingState !== 'closed') closeConnection(uid)
    });

    groupID = null;
    offerRef = null;
    answerRef = null;
    connections = {};
    ICELists = {};

    showJoinGroup();
    console.log("leave group complete")
}

window.addEventListener("beforeunload", leaveGroup);

function createRTCConnection(uid) {
    /**
     * Creates an RTC Connection object for a given database entry.
     */

    //create connection
    console.log('Making a new connection for ' + uid + '.');
    const connection = new RTCPeerConnection();
    connections[uid] = connection;
    ICELists[uid] = [];
    connection.onicecandidate = onGenerateICE;

    connection.addStream(webcamStream);

    return connection;
}

async function makeOffer(childSnapshot) {
    /**
     * Initiates a new connection with a given user and sends them an offer.
     */
    const uid = childSnapshot.key;
    if(uid == ourID) return;

    const connection = createRTCConnection(uid);

    console.log("Sending offer to " + uid + '...');
    const offer = await connection.createOffer();
    database.ref('/groups/' + groupID + '/joined/' + uid + '/offers/' + ourID)
        .set(JSON.stringify(offer));
    await connection.setLocalDescription(offer);
}

async function onReceiveOffer(snapshot) {
    const uid = snapshot.key;
    const offer = JSON.parse(snapshot.val());

    const connection = createRTCConnection(uid);
    await connection.setRemoteDescription(offer);

    // TODO: Figure out how to remove offer with out deleting tree
    // console.log(offerRef.removeChild());
    // offerRef.child(uid).set(null);

    console.log("Offer accepted from " + uid +". Sending our answer.");
    let answer = await connection.createAnswer();
    connection.setLocalDescription(answer);
    database.ref('/groups/' + groupID + '/joined/' + uid + '/answers/' + ourID)
        .set(JSON.stringify(answer));

    openVideoStream(uid);
}

async function onReceiveAnswer(snapshot) {
    const uid = snapshot.key;
    const answer = JSON.parse(snapshot.val());

    await connections[uid].setRemoteDescription(answer);
    console.log("Answer accepted from " + uid + ".");
    //delete answer

    openVideoStream(uid);
    //register any ice candidates we might have received up to now and had ignored
    onReceiveICE(await database.ref('/groups/' + groupID + '/joined/' + ourID + '/ice/' + uid).once('value'));
}

function onClose(snapshot){
    const uid = snapshot.key;
    closeVideoStream(uid);
    connections[uid].close();
    //connections[uid] = null; //we're supposed to do this, but it crashes things
}

function onGenerateICE(event) {
    var callerUID;
    for( uid in connections) {
        if(connections[uid] == event.target) callerUID = uid;
    }
    ICEList = ICELists[callerUID];
    if(event.candidate) {
        ICEList.push(event.candidate)
    } else {
        console.log("Sending ICE candidates");
        database.ref("/groups/" + groupID + "/joined/" + callerUID + "/ice/" + ourID).set(JSON.stringify(ICEList));
    }
}

function onReceiveICE(snapshot) {
    const uid = snapshot.key;

    // Check if we have a connection object with this host and if the host is registered.
    // If we haven't done that yet, we can skip it now and pick it up later
    if(!connections.hasOwnProperty(uid)) return;
    const connection = connections[uid];

    if(connection.remoteDescription == null) return;

    console.log(snapshot.val());
    if(snapshot.val() == null) {
        console.log("received a null ice list??");
        return;
    }

    const ICEList = JSON.parse(snapshot.val());
    for(candidate of ICEList) {
        connection.addIceCandidate(candidate);
    }
}

function closeConnection(key) {
    console.log("closing connection to "+ key);
    closeVideoStream(uid);
    connections[key].close();
    database.ref('/groups/' + groupID + /joined/ + key + /closing/ + ourID ).set("");
}


//UI Functions
function openVideoStream(uid) {
    const remoteStream = connections[uid].getRemoteStreams()[0];
    const display = document.createElement("video");
    display.id = uid;
    display.autoplay = true;
    display.playsinline = true;
    display.srcObject = remoteStream;
    document.getElementById("videos").appendChild(display);
}

function closeVideoStream(uid) {
    try {
        document.getElementById(uid).remove();
    } catch (e) {}
}

function showJoinGroup() {
    try {
        document.getElementById("group_label").remove();
        document.getElementById("leave").remove()
    } catch (e) {}

    const div = document.getElementById("group_controls");

    const number = document.createElement("input");
    number.id = "grp";
    number.type = "number";

    const join = document.createElement("button");
    join.id = "join";
    join.onclick = joinGroup;
    join.innerText = "Join";

    div.appendChild(number);
    div.appendChild(join);
}

function showLeaveGroup() {
    document.getElementById("grp").remove();
    document.getElementById('join').remove();

    const div = document.getElementById("group_controls");

    const currentGroup = document.createElement("h2");
    currentGroup.id = "group_label";
    currentGroup.innerText = "Current group: " + groupID;

    const leave = document.createElement("button");
    leave.id = "leave";
    leave.onclick = leaveGroup;
    leave.innerText = "Leave current group";

    div.appendChild(currentGroup);
    div.appendChild(leave);
}
