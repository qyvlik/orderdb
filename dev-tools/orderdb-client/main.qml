import QtQuick 2.8
import QtQuick.Window 2.2

import QtWebSockets 1.0
import QtQuick.Controls 2.2
import QtQuick.Layouts 1.3


ApplicationWindow {
    width: 640
    height: 720
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

        TextField {
            Layout.fillWidth: true
            id: groupInput
            text: "test"
            placeholderText: "input group"
        }

        RowLayout {
            Layout.fillWidth: true
            TextField {
                id: indexInput
                Layout.fillWidth: true
                placeholderText: "input index"
            }

            Button {
                Layout.fillWidth: true
                text: "get.by.index"
                onClicked: {
                    orderDBGetByIndex(groupInput.text, indexInput.text, function(res){
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
                    orderDBGetByKey(groupInput.text, keyInput.text, function(res){
                        console.log("res:" + JSON.stringify(res))
                    });
                }
            }
        }

        RowLayout {
            Layout.fillWidth: true
            TextField {
                id: fromInput
                Layout.fillWidth: true
                placeholderText: "from"
            }

            TextField {
                id: toInput
                Layout.fillWidth: true
                placeholderText: "to"
            }


            Button {
                Layout.fillWidth: true
                text: "get.by.key"
                onClicked: {
                    orderDBGetList(groupInput.text, fromInput.text, toInput.text, function(res){
                        console.log("res:" + JSON.stringify(res))
                    });
                }
            }
        }

        Button {
            id: subscribeButton
            Layout.fillWidth: true
            text: "subscribe:" + subscribeButton.subscribe
            property bool subscribe: false
            onClicked: {
                subscribeButton.subscribe = !subscribeButton.subscribe;
                orderDBSubAppend(groupInput.text, subscribeButton.subscribe, function(res){
                    if (res.error) {
                        console.error("orderDBSubAppend error:" + JSON.stringify(res));
                        return;
                    }

                    if (res.result === 'subscribe') {
                        return;
                    }

                    console.info("orderDBSubAppend: " + JSON.stringify(res));
                });
            }
        }

        Button {
            Layout.fillWidth: true
            text: "get latest index"
            onClicked: {
                orderDBGetLatestIndex(groupInput.text, function(res){
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
        property int  currentId: 1
        interval: 50
        running: false
        repeat: true
        onTriggered: {
            var size = 1000;
            console.time("batchSeq")
            batchSeq(currentId, size);
            currentId += size;
            console.timeEnd("batchSeq")
        }
    }

    function batchSeq(currentId, count) {
        while(count -- > 0) {
            orderDBAppend(groupInput.text,
                            "submit-" + (currentId+count),
                            {
                                id: currentId+count,
                                price: "1000.0",
                                amount: "1.0"
                            }, function(){}
                            );
        }
    }

    function orderDBGetList(group, from, to, callback) {
        var params = [group, from, to, ];
        readerClient.callRpcMethod("get.list", params, callback);
    }

    function orderDBGetLatestIndex(group, callback) {
        var params = [group];
        readerClient.callRpcMethod("get.latest.index", params, callback);
    }

    function orderDBGetByKey(group, key, callback) {
        var params = [group, key];
        readerClient.callRpcMethod("get.by.key", params, callback);
    }

    function orderDBGetByIndex(group, index, callback) {
        var params = [group, index];
        readerClient.callRpcMethod("get.by.index", params, callback);
    }

    function orderDBAppend(group, key, value, callback) {
        var params =  [group, key, value];
        writeClient.callRpcMethod("append", params, callback)
    }

    function orderDBSubAppend(group, subscribe, callback) {
        var params = [group];
        readerClient.subChannel("sub.append", params, subscribe, callback);
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
