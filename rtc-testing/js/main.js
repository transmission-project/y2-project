const localVideo = document.querySelector('#localVideo');
showJoinGroup();

let database = firebase.database();

let groupID; // ID of joined group
const ourID = Math.floor(Math.random() *90000) + 10000;// Our user ID, randomly generated for tests

let offerRef; // Incoming offer handler, is a firebase listen object
let answerRef; // Incoming answer handler, is a firebase listen object
let closeRef; //TODO comments
let iceRef;

let connections = {};
let ICELists = {};

// TODO: replace stream with track
// init webcam
let webcamStream;
navigator.mediaDevices.getUserMedia({
      audio: true,
      video: true
}).then( (stream) => { // I have no idea how to use tracks, so I use streams even though they're depreciated
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
    closeRef.on('child_added', onReceiveICE);

    //add ourselves to database
    await database.ref('/groups/' + groupID + '/joined/' + ourID).set("");
    console.log("Joined group as "+ ourID + ".");

    // Make connections to users already in group
    database.ref('/groups/'+ groupID + '/joined')
        .once('value', (snapshot) => {
            console.log(snapshot);
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
    //TODO: ICE

    //close rtc connections
    Object.keys(connections).forEach((uid) => {
        if(connections[uid].signalingState !== 'closed') closeConnection(uid)
    });

    groupID = null;
    offerRef = null;
    answerRef = null;
    connections = {};
    ICELists = {};

    showJoinGroup();
}

window.onbeforeunload = leaveGroup;

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

    //stream handlers
    connection.onaddstream = (event) => {
        console.log('Remote stream added.');
        const remoteStream = event.stream;
        const display = document.createElement("video");
        display.id = uid;
        display.autoplay = true;
        display.playsinline = true;
        display.srcObject = remoteStream;
        document.getElementById("videos").appendChild(display);
    };
    connection.onremovestream = (event) => {
        console.log('Remote stream removed. Event: ', event);
        document.getElementById(uid).remove();
    };
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

    console.log("Offer accepted. Sending answer to " + uid + "...");
    let answer = await connection.createAnswer();
    connection.setLocalDescription(answer);
    database.ref('/groups/' + groupID + '/joined/' + uid + '/answers/' + ourID)
        .set(JSON.stringify(answer));

    //register any ice candidates we might have received up to now and had ignored
    onReceiveICE(await database.ref('/groups/' + groupID + '/joined/' + ourID + '/ice/' + uid).once('value'));
}

async function onReceiveAnswer(snapshot) {
    const uid = snapshot.key;
    const answer = JSON.parse(snapshot.val());

    await connections[uid].setRemoteDescription(answer);
    console.log("Answer accepted from " + uid + ". Preparing to send ICE candidates.")
    //delete answer

    //register any ice candidates we might have received up to now and had ignored
    onReceiveICE(await database.ref('/groups/' + groupID + '/joined/' + ourID + '/ice/' + uid).once('value'));
}

function onClose(snapshot){
    const uid = snapshot.key;
    connections[uid].close();
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
        database.ref("/groups/" + groupID + "/joined/" + callerUID + "/ice/" + ourID).set(JSON.stringify(ICEList));
    }
}

function onReceiveICE(snapshot) {
    const uid = snapshot.key;

    // Check if we have a connection object with this host and if the host is registered.
    // If we haven't done that yet, we can skip it now and pick it up later
    if(!connections.hasOwnProperty(uid)) return;
    const connection = connections[uid];

    console.log("ICE: checking signalling");
    if(connection.signalingState === 'closed') return;
    console.log("ICE: checking remoteDescription");
    if(connection.remoteDescription == null) return;
    //if() return;

    console.log(snapshot.val());
    const ICEList = JSON.parse(snapshot.val());
    console.log(ICEList);
    for(candidate of ICEList) {
        if(connection.signalingState !== "closed") {
            connection.addIceCandidate(candidate);
        }
    }
}

function closeConnection(key) {
    connections[key].close();
    database.ref('/groups/' + groupID + /joined/ + key + /closing/ + ourID ).set("");
}


//UI Functions
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
