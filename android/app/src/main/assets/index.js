let database = firebase.database();

const groupID = androidInterface.getGroupID(); // ID of joined group
const ourID = androidInterface.getUid();// Our user ID

let offerRef; // Incoming offer handler, is a firebase listen object
let answerRef; // Incoming answer handler, is a firebase listen object
let closeRef; // Incoming Closing connection handler, is a firebase listen object
let iceRef; // Incoming IceCandidates handler, is a firebase listen object

let connections = {};
let ICELists = {};

// TODO: replace stream with track
// init webcam
let LocalStream;
navigator.mediaDevices.getUserMedia({
      audio: true,
      video: false
}).then( (stream) => { // I have no idea how to use tracks, so I use streams even though they're depreciated
        stream.getTracks()[0].enabled = false; //mute mic by default
        LocalStream = stream;
        joinGroup();
});

async function joinGroup() {
    //start listening to offers, answers, and closes
    offerRef = database.ref('/groups/' + groupID + '/rtc/' + ourID + '/offers');
    offerRef.on('child_added', onReceiveOffer);
    answerRef = database.ref('/groups/' + groupID + '/rtc/' + ourID + '/answers');
    answerRef.on('child_added', onReceiveAnswer);
    closeRef = database.ref('/groups/' + groupID + '/rtc/' + ourID + '/closing');
    closeRef.on('child_added', onClose);
    iceRef = database.ref('/groups/' + groupID + '/rtc/' + ourID + '/ice');
    iceRef.on('child_added', onReceiveICE);

    //add ourselves to database
    console.log("Joined group as "+ ourID + ".");

    // Make connections to users already in group
    database.ref('/groups/'+ groupID + '/joined')
        .once('value', (snapshot) => {
            snapshot.forEach((childSnapshot) => {
                console.log(childSnapshot); // Having this line here solves race conditions for some reason
                makeOffer(childSnapshot);
            })
        });
}

async function leaveGroup() {
    await database.ref('/groups/' + groupID + '/rtc/' + ourID).set(null);

    //unsubscribe from listeners
    offerRef.off();
    answerRef.off();
    closeRef.off();
    iceRef.off();

    //close rtc connections
    Object.keys(connections).forEach((uid) => {
        if(connections[uid].signalingState !== 'closed') { //Ignore already closed connections
            console.log("closing connection to "+ uid);
            connections[uid].close();
            database.ref('/groups/' + groupID + "/rtc/" + uid + "/closing/" + ourID ).set("");
        }
        removeAudioElement(uid); //remove all the users we listed to html, even if we forgot them earlier
    });

    groupID = null;
    offerRef = null;
    answerRef = null;
    connections = {};
    ICELists = {};

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

    connection.addStream(LocalStream);

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
    database.ref('/groups/' + groupID + '/rtc/' + uid + '/offers/' + ourID)
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
    database.ref('/groups/' + groupID + '/rtc/' + uid + '/answers/' + ourID)
        .set(JSON.stringify(answer));

    addAudioElement(uid);
}

async function onReceiveAnswer(snapshot) {
    const uid = snapshot.key;
    const answer = JSON.parse(snapshot.val());

    await connections[uid].setRemoteDescription(answer);
    console.log("Answer accepted from " + uid + ".");
    //delete answer

    addAudioElement(uid);
    //register any ice candidates we might have received up to now and had ignored
    onReceiveICE(await database.ref('/groups/' + groupID + '/rtc/' + ourID + '/ice/' + uid).once('value'));
}

function onClose(snapshot){
    const uid = snapshot.key;
    removeAudioElement(uid);
    connections[uid].close();
    //connections[uid] = null; //TODO: we're supposed to do this, but it crashes things
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
        database.ref("/groups/" + groupID + "/rtc/" + callerUID + "/ice/" + ourID).set(JSON.stringify(ICEList));
    }
}

function onReceiveICE(snapshot) {
    const uid = snapshot.key;

    // Check if we have a connection object with this host and if the host is registered.
    // If we haven't done that yet, we can skip reading ICE for now and come back in onRecieveAnswer()
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

//HunterTalk App Interface functions
function startTalking() {
    LocalStream.getTracks()[0].enabled = true
}

function stopTalking() {
    LocalStream.getTracks()[0].enabled = false
}


//Audio element functions
function addAudioElement(uid) {
    const remoteStream = connections[uid].getRemoteStreams()[0];
    const listItem = document.createElement("li");
    listItem.id = uid;
    listItem.innerText = uid;
    const audio = document.createElement("audio")
    audio.srcObject = remoteStream;
    audio.autoplay = true;
    listItem.appendChild(audio);
    document.getElementById("connected").appendChild(listItem);
}

function removeAudioElement(uid) {
    try {
        document.getElementById(uid).remove();
    } catch (e) {}
}
