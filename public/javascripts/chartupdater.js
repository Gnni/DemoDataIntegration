var websocketErp;
var websocketErpName = "websocketErp";
var websocketIoT;
var websocketIoTName = "websocketIoT";
var websocketIssues;
var erpChart = "erp_data";
var iotChart = "iot_data";

function connectToWs() {
    updateChart("getnnewrows/0", iotChart, websocketIoTName);
    updateChart("getnnewrows/1", erpChart, websocketErpName);
}

var closeFunction = function (evt) {
    console.log("closing websocket.");
};
var openFunction = function (evt) {
    console.log("opening websocket.");
};
var errorFunction = function (evt) {
    console.error("Error! " + evt);
};

function updateChart(route, chart, websocket) {
    chart = $('#' + chart).highcharts();
    if (window[websocket] !== undefined) {
        window[websocket].close();
    } //192.168.30.208
    window[websocket] = new WebSocket("ws://192.168.30.208:9000/" + route);
    window[websocket].onmessage = function (event) {
        var shift = chart.series[0].data.length > 15;
        chart.series[0].addPoint(
            [(new Date()).getTime(),
              JSON.parse(event.data)], true, shift );
    }
    window[websocket].onopen = openFunction;
    window[websocket].onclose = closeFunction;
    window[websocket].onerror = errorFunction;
}

function pauseDiagramUpdates() {
    var stopMsg = "0";
    sendMsgToDiagramSockets(stopMsg);
}

function activateDiagramUpdates() {
    var startMsg = "1000";
    sendMsgToDiagramSockets(startMsg);
}

function sendMsgToDiagramSockets(msg) {
    sendMsgToSocket(websocketErpName, msg);
    sendMsgToSocket(websocketIoTName, msg);
    //sendMsgToSocket(websocketIssues, msg);
}

function sendMsgToSocket(socketName, msg) {
    window[socketName].send(msg);
}

function closeConn() {
    if (websocketErp !== undefined) {
        websocketErp.close();
    }
    if (websocketIoT !== undefined) {
        websocketIoT.close();
    }
    if (websocketIssues !== undefined) {
        websocketIssues.close();
    }
}
