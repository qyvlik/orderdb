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

        TextField {
            id: idInput
            Layout.fillWidth: true
            placeholderText: "input sequenceId"
        }

        Button {
            Layout.fillWidth: true
            text: "orderdb.get.value.by.sequenceId"
            onClicked: {
                orderDBGetValueBySequence("test", idInput.text, function(res){
                    console.log("res:" + JSON.stringify(res))
                });
            }
        }

//        Button {
//            Layout.fillWidth: true
//            text: "orderdb.sequence"
//            onClicked: {
//                orderDBSequence("test",
//                                "submit-" + idInput.text,
//                                {
//                                    id: idInput.text,
//                                    price: "1000.0",
//                                    amount: "1.0"
//                                },
//                                function(res){
//                                    console.log("orderdb.sequence:" + JSON.stringify(res));
//                                });
//            }
//        }

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
        property int  currentId: 1000000
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


    function orderDBGetValueByKey(group, key, callback) {
        var params = [group, key];
        readerClient.callRpcMethod("get.value.by.key", params, callback);
    }

    function orderDBGetValueBySequence(group, sequenceId, callback) {
        var params = [group, sequenceId];
        readerClient.callRpcMethod("get.value.by.sequence", params, callback);
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
