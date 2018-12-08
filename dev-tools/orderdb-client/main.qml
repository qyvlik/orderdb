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
            onClicked: {
                writeClient.active = !writeClient.active;
                readerClient.active = !readerClient.active;
            }
        }

        RowLayout {
            Layout.fillWidth: true
            TextField {
                id: sequenceInput
                Layout.fillWidth: true
                placeholderText: "input sequence"
            }

            Button {
                Layout.fillWidth: true
                text: "get.by.sequence"
                onClicked: {
                    orderDBGetBySequence("test", sequenceInput.text, function(res){
                        console.log("res:" + JSON.stringify(res))
                    });
                }
            }
        }

        RowLayout {
            Layout.fillWidth: true
            TextField {
                id: keyInput
                Layout.fillWidth: true
                placeholderText: "input key"
            }

            Button {
                Layout.fillWidth: true
                text: "get.by.key"
                onClicked: {
                    orderDBGetByKey("test", keyInput.text, function(res){
                        console.log("res:" + JSON.stringify(res))
                    });
                }
            }
        }

        Button {
            Layout.fillWidth: true
            text: "get latest sequence"
            onClicked: {
                orderDBGetLatestSequence("test", function(res){
                    console.log("res:" + JSON.stringify(res))
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
        property int  currentId: 16
        interval: 50
        running: false
        repeat: true
        onTriggered: {
            var size = 500;
            console.time("batchSeq")
            batchSeq(currentId, size);
            currentId += size;
            console.timeEnd("batchSeq")
        }
    }

    function batchSeq(currentId, count) {
        while(count -- > 0) {
            orderDBSequence("test",
                            "submit-" + currentId+count,
                            {
                                id: currentId+count,
                                price: "1000.0",
                                amount: "1.0"
                            }, function(){}
                            );
        }
    }


    function orderDBGetLatestSequence(group, callback) {
        var params = [group];
        readerClient.callRpcMethod("get.latest.sequence", params, callback);
    }

    function orderDBGetByKey(group, key, callback) {
        var params = [group, key];
        readerClient.callRpcMethod("get.by.key", params, callback);
    }

    function orderDBGetBySequence(group, sequenceId, callback) {
        var params = [group, sequenceId];
        readerClient.callRpcMethod("get.by.sequence", params, callback);
    }

    function orderDBSequence(group, key, value, callback) {
        var params =  [group, key, value];
        writeClient.callRpcMethod("sequence", params, callback)
    }

    RpcClient {
        id: writeClient
        url: "ws://localhost:17711/orderdb"
    }

    RpcClient {
        id: readerClient
        url: "ws://localhost:17711/orderdb"
    }

}
