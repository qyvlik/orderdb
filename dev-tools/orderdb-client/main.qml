import QtQuick 2.8
import QtQuick.Window 2.2

import QtWebSockets 1.0
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3


ApplicationWindow {
    width: 360
    height: 680
    visible: true
    title: qsTr("orderdb-client")


    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 10

        Button {
            Layout.fillWidth: true
            text: "open"
            onClicked: socket.active = !socket.active
        }

        TextField {
            id: idInput
            Layout.fillWidth: true
            placeholderText: "input id"
        }

        Button {
            Layout.fillWidth: true
            text: "orderdb.sequence"
            onClicked: {
                orderDBSequence("test",
                                "submit-" + idInput.text,
                                {
                                    id: idInput.text,
                                    price: "1000.0",
                                    amount: "1.0"
                                },
                                function(res){
                                    console.log("orderdb.sequence:" + JSON.stringify(res));
                                });
            }
        }

        Button {
            Layout.fillWidth: true
            text: "timer:" + timer.running
            onClicked: timer.running = !timer.running
        }

        Item {
            Layout.fillHeight: true
        }
    }

    Timer {
        id: timer
        property int  currentId: 300000
        interval: 50
        running: false
        repeat: true
        onTriggered: {
            console.time("batchSeq")
            batchSeq(currentId, 1000);
            currentId+=100;
            console.timeEnd("batchSeq")
        }
    }

    function batchSeq(currentId, count) {
        var seqCB = function(res){
            orderDBGetValueBySequenceId("test", res.result, function(res){
                // console.log("orderDBGetValueBySequenceId:" + JSON.stringify(res));
            })
        };

        while(count -- > 0) {
            orderDBSequence("test",
                            "submit-" + currentId+count,
                            {
                                id: currentId+count,
                                price: "1000.0",
                                amount: "1.0"
                            },
                            seqCB);
        }
    }


    function orderDBGetValueBySequenceId(group, sequenceId, callback) {
        var params = [group, sequenceId];
        callRpcMethod("orderdb.get.value.by.sequenceId", params, callback);
    }


    function orderDBSequence(group, key, value, callback) {
        var params =  [group, key, value];
        callRpcMethod("orderdb.sequence", params, callback)
    }

    readonly property var rpcCallback:({});
    readonly property var channelCallback:({});

    function callRpcMethod(method, params, callback) {
        var id = (new Date()).getTime();

        var req = {
            id: id,
            method: method,
            params: params
        }

        callback = callback || function(res) {
            console.log(JSON.stringify(res));
        };

        rpcCallback[id] = (function(response){
            callback(response);
        });

        socket.sendTextMessage(JSON.stringify(req));
    }

    function subChannel(channel, params, subscribe, callback) {
        subscribe = subscribe || true;
        callback = callback || function(res) {
            console.log(JSON.stringify(res));
        };

        var req = {
            channel: channel,
            params: params,
            subscribe: subscribe
        }

        channelCallback[channel] = callback;

        socket.sendTextMessage(JSON.stringify(req));
    }

    WebSocket {
        id: socket
        url: "ws://localhost:8081/orderdb"
        onTextMessageReceived: {

            var obj = JSON.parse(message);

            if (typeof obj.channel !== 'undefined') {

                var channelCB = channelCallback[obj.channel];
                if (typeof channelCB !== 'undefined') {
                    channelCB(obj);
                } else {
                    console.error("have not channel:" + obj.channel + " callback")
                }

            }

            if (typeof obj.id !== 'undefined') {
                var rpcCB = rpcCallback[obj.id];
                if (typeof rpcCB !== 'undefined') {
                    rpcCB(obj);
                } else {
                    console.error("have not id:" + obj.id + " callback, method: "  + obj.method)
                }
            }
        }
    }

}
