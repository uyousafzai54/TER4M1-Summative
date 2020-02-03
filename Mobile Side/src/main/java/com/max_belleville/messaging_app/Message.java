package com.max_belleville.messaging_app;

public class Message {
        //The message model.

        private String text; // message
        private boolean belongsToCurrentUser; // is this message sent by us?
        private String date="MARCH?";//get date of message
        public Message(String text, boolean belongsToCurrentUser) {
            this.text = text;
            this.belongsToCurrentUser = belongsToCurrentUser;
        }
        public String getText() {
            return text;
        }
        public void setDate(String date){
            this.date=date;
        }
        public String getDate(){
            return date;
        }
        public boolean isBelongsToCurrentUser() {
            return belongsToCurrentUser;
        }
    }