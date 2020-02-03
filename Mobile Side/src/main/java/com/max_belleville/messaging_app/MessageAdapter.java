package com.max_belleville.messaging_app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


// MessageAdapter.java
public class MessageAdapter extends BaseAdapter {

    List<Message> messages = new ArrayList<Message>();
    Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        //Add new message to messages notify adapter that there is a new message.

        message.setDate(getTime());
        this.messages.add(message);
        notifyDataSetChanged();
    }
    private String getTime(){
        //Get current time on phone.

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MMMM, dd h:mm a");
        String formattedDate = df.format(c);
        return formattedDate;
    }
    public void saveInfo () {
        String filename = "MessagingApp";
        FileOutputStream outputStream;
        //Open internal storage file, loop through all messages and write messages to file

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            for(int i=0;i<messages.size();i++){
                int val = messages.get(i).isBelongsToCurrentUser() ? 0 : 1;
                String date = messages.get(i).getDate();
                String message = messages.get(i).getText();
                outputStream.write((val+"\t"+date+"\t"+message+"\n").toString().getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadInfo(){
        String filename = "MessagingApp";
        FileInputStream inputStream;
        //Open file from internal storage read each line of the file get date, user state and message
        //Add new messages to messages array
        try {
            inputStream = context.openFileInput(filename);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            for (String line; (line = r.readLine()) != null; ) {
                String[] text =line.split("\t");
                boolean val =(text[0].equals("1")) ? false : true;
                messages.add(new Message(text[2],val));
                messages.get(messages.size()-1).setDate(text[1]);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        //Things for the ui of listView

        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        //Get all messsages in list view

        Message message = messages.get(i);

        //Get a text object depending on the user (left for us, right for them)

        if (message.isBelongsToCurrentUser())
            convertView = messageInflater.inflate(R.layout.my_messages, null);
         else
            convertView = messageInflater.inflate(R.layout.their_messages, null);
         //Update message and date for that text object

         holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
        holder.date = (TextView) convertView.findViewById(R.id.date);
        convertView.setTag(holder);
        holder.messageBody.setText(message.getText());
        holder.date.setText(message.getDate());
        //Return text object

        return convertView;
    }

}

class MessageViewHolder {
    public TextView messageBody;
    public TextView date;
}