let database = firebase.database();
const localVideo = document.querySelector('#localVideo');
let groupID; // ID of joined group
let ourID; // Our user ID, randomly generated for tests

let offerRef; // Incoming offer handler, is a firebase listen object
let answerRef; // Incoming answer handler, is a firebase listen object

let connections = {};

// commented out because asking for webcam is annoying
// init webcam
// navigator.mediaDevices.getUserMedia({
//       audio: false,
//       video: true
// }).then( (stream) => { // I have no idea how to use tracks, so I use streams even though they're depreciated
//         localVideo.srcObject = stream;
//         localConnection.addStream(stream);
//     }).catch(function(e) {
//         alert("Oh no!\n" + e)
// });

function joinGroup() {
    groupID = document.getElementById('grp').value;
    ourID = Math.floor(Math.random() *10001);

    //add ourselves to database

    //start listening to offers and answers
    offerRef = database.ref('/groups/' + groupID + '/joined/' + ourID + 'offers');
    offerRef.on('child_added', onReceiveOffer);
    answerRef = database.ref('/groups/' + groupID + '/joined/' + ourID + 'answers');
    answerRef.on('child_added', onReceiveAnswer);

    // Make connections to users already in group
    database.ref('/groups/'+ groupID + '/joined')
        .once('value', (snapshot) => {
            snapshot.forEach(makeOffer)
        });
}

function leaveGroup() {
    // remove self from database

    offerRef.off();
    answerRef.off();

    //close rtc connections

    groupID = null;
    ourID = null;
    offerRef = null;
    answerRef = null;
    connections = {};
}

function createRTCConnection(uid) {
    /**
     * Creates an RTC Connection object for a given database entry.
     */

    //create connection
    console.log('Making a new connection for ' + uid + '.');
    const connection = new RTCPeerConnection();
    connections[uid] = connection;

    //stream handlers
    connection.onaddstream = (event) => {
        console.log('Remote stream added.');
        remoteStream = event.stream;

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

    const connection = createRTCConnection(uid)

    console.log("Sending offer to " + uid + '...');
    const offer = await connection.createOffer(null);
    database.ref('/groups/' + groupId + '/joined/' + uid + '/offers/' + ourID)
        .set(JSON.stringify(offer));
    connection.setLocalDescription(offer);
}

function onReceiveOffer() {
    //TODO: do we need to pick the right child here? what do we get
    const uid = childSnapshot.key;
    if(uid == ourID) return;

    const connection = createRTCConnection(uid);

    //register offer, and make and send answer
    // update ice candidates
    // delete offer
}

function onReceiveAnswer() {
    //TODO: Mirror onReceiveOffer here
    //accept answer
    //update ice candidates
    //delete answer
}




///////old functions/////////


// localConnection.onicecandidate = ICEQueueAndThenLog;


async function recieveOffer() {
  let offer = await database.ref('offer').once('value');
  offer = JSON.parse(offer.node_.value_);
  console.log(offer);
  localConnection.setRemoteDescription(offer);
  let answer = await localConnection.createAnswer();
  console.log(answer);
  localConnection.setLocalDescription(answer);
  database.ref('answer').set(JSON.stringify(answer));
}

async function recieveAnswer() {
  const snapshot = await database.ref('answer').once('value');
  const remote = JSON.parse(snapshot.node_.value_);
  console.log(remote);
  localConnection.setRemoteDescription(remote);
}
//////

let ICEList = [];
function ICEQueueAndThenLog(event) {
  if(event.candidate) {
    ICEList.push({
        sdpMLineIndex: event.candidate.sdpMLineIndex,
        candidate: event.candidate.candidate
    })
  }
}

function uploadICE(num) {
    database.ref('ice' + num).set(null);
    database.ref('ice' + num).set(JSON.stringify(ICEList));
}

/// User-facing fn
async function addICECands(num) {
  ICERList = await database.ref('ice' + num).once('value');
    ICERList = JSON.parse(ICERList.node_.value_);
  console.log(ICERList);
  for(candidateMsg of ICERList) {
    candidate = new RTCIceCandidate(candidateMsg);
    localConnection.addIceCandidate(candidate);
  }
}