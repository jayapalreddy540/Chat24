/*
 * Functions SDK : is required to work with firebase functions.
 * Admin SDK : is required to send Notification using functions.
 */

'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


/*
 * 'OnWrite' works as 'addValueEventListener' for android. It will fire the function
 * everytime there is some item added, removed or changed from the provided 'database.ref'
 * 'sendNotification' is the name of the function, which can be changed according to
 * your requirement
 */


exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((change, context) => {

    /*
     * You can store values as variables from the 'database.ref'
     * Just like here, I've done for 'user_id' and 'notification'
     */

    const user_id = context.params.user_id;
    const notification_id = context.params.notification_id;

    console.log("we have a notificaion to send to : ", user_id);

    /*
     * Stops proceeding to the rest of the function if the entry is deleted from database.
     * If you want to work with what should happen when an entry is deleted, you can replace the
     * line from "return console.log.... "
     */

    if (!change.after.val()) {
        return console.log('A notification has been deleted from the database : ', notification_id);
    }

    /*
     * 'fromUser' query retreives the ID of the user who sent the notification
     */


    const fromUser = admin.database().ref(`/notifications/${user_id}/${notification_id}`).once('value');
    return fromUser.then(fromUserResult => {
        const from_user_id = fromUserResult.val().from;
        console.log('You have new notification from : ', from_user_id);

        /*
         * The we run two queries at a time using Firebase 'Promise'.
         * One to get the name of the user who sent the notification
         * another one to get the devicetoken to the device we want to send notification to
         */


        const userQuery = admin.database().ref(`/users/${from_user_id}/name`).once('value');
        const deviceToken = admin.database().ref(`/users/${user_id}/device_token`).once('value');

        return Promise.all([userQuery, deviceToken])
            .then(result => {
                const userName = result[0].val();
                const token_id = result[1].val();
                /*
                 * We are creating a 'payload' to create a notification to be sent.
                 */

                const payload = {
                    notification: {
                        title: " New Friend Request",
                        body: `${userName} has sent you a Friend Request`,
                        icon: "default",
                        click_action: "tk.codme.chat24_TARGET_NOTIFICATION",
                    },
                    data: {
                        user_id: from_user_id
                    }
                };

                /*
                 * Then using admin.messaging() we are sending the payload notification to the token_id of
                 * the device we retreived.
                 */
                return admin.messaging().sendToDevice(token_id, payload)
                    .then(response => {
                        return console.log('This is the notification feature ', response);
                    });
            });
    });
});



exports.sendMessageNotification = functions.database.ref('/messages/{user_from_id}/{user_to_id}/{message_id}').onWrite((change, context) => {


    const user_from_id = context.params.user_from_id;
    const user_to_id = context.params.user_to_id;
    const message_id = context.params.message_id;

    console.log("we have a notificaion to send to : ", user_from_id);


    if (!change.after.val()) {
        return console.log('A message has been deleted from the database : ', message_id);
    }

    const fromUser = admin.database().ref(`/messages/${user_from_id}/${user_to_id}/${message_id}`).once('value');
    return fromUser.then(fromUserResult => {
        const from_user_id = fromUserResult.val().from;
        const message = fromUserResult.val().message;
        const type = fromUserResult.val().type;

        console.log('You have new message from : ', from_user_id);
        if (from_user_id === user_to_id) {
            return;
        }
        const userQuery = admin.database().ref(`/users/${from_user_id}/name`).once('value');
        const deviceToken = admin.database().ref(`/users/${user_to_id}/device_token`).once('value');
        //const image = admin.storage().ref(`profile_images/${from_user_id}.jpg`).once('value');

        return Promise.all([userQuery, deviceToken])
            .then(result => {
                const userName = result[0].val();
                const token_id = result[1].val();
                /*
                 * We are creating a 'payload' to create a notification to be sent.
                 */
                const payload1 = {
                    notification: {
                        title: `${userName}`,
                        body: `${message}`,
                        icon: "default",
                        click_action: "tk.codme.chat24_MESSAGE_NOTIFICATION",
                    },
                    data: {
                        user_id: `${from_user_id}`,
                        user_name: `${userName}`
                    }
                };


                /*
                 * Then using admin.messaging() we are sending the payload notification to the token_id of
                 * the device we retreived.
                 */
                return admin.messaging().sendToDevice(token_id, payload1)
                    .then(response => {
                        return console.log('this is message notification feature ', response);
                    });
            });
        // }
    });

});