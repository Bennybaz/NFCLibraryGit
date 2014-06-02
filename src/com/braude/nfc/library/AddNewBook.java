package com.braude.nfc.library;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.*;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Benny on 20/03/2014.
 */
public class AddNewBook extends Activity{

    final Context context = this;
    private Button writeBtn;
    private Spinner field;
    private TextView inst;
    TextView message;
    Dialog dialog;
    NfcAdapter adapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    private int writeBtnClicked=0;
    boolean writeMode; //flag for enable/disable write on tag
    Tag mytag; //the NFC tag for write
    Context ctx;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_book);
        ctx=this;

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Manage Tags");
        writeBtn = (Button) findViewById(R.id.writeNfcBtn);
        message = (TextView)findViewById(R.id.newBookET);
        field = (Spinner) findViewById(R.id.typeOfTagSpinner);
        inst = (TextView) findViewById(R.id.textView2newBook);

        field.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0) inst.setText("Enter Book Code:");
                if(position==1) inst.setText("Enter Shelf Number:");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // add button listener
        writeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (message.getText().toString().equals(""))
                    Toast.makeText(context, "Please Enter an Input", Toast.LENGTH_SHORT).show();
                else {
                    if (field.getSelectedItemPosition() == 0) {
                        String pat = "([0-9]+[-][0-9]+)|([0-9]+)";
                        Pattern pattern = Pattern.compile(pat);
                        Matcher matcher = pattern.matcher(message.getText().toString());
                        if (matcher.matches()) {
                            // custom dialog
                            writeBtnClicked = 1;
                            dialog = new Dialog(context);
                            dialog.setContentView(R.layout.dialog_write);
                            dialog.setTitle("Write on NFC Tag");
                            Button dialogButton = (Button) dialog.findViewById(R.id.write_cancel_button);
                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.show();
                        } else Toast.makeText(context, "Wrong Input Pattern", Toast.LENGTH_SHORT).show();
                    }

                        if (field.getSelectedItemPosition() == 1) {
                            String pat2 = "[0-9]+";
                            Pattern pattern2 = Pattern.compile(pat2);
                            Matcher matcher2 = pattern2.matcher(message.getText().toString());
                            if (matcher2.matches()) {
                                // custom dialog
                                writeBtnClicked = 1;
                                dialog = new Dialog(context);
                                dialog.setContentView(R.layout.dialog_write);
                                dialog.setTitle("Write on NFC Tag");
                                Button dialogButton = (Button) dialog.findViewById(R.id.write_cancel_button);
                                // if button is clicked, close the custom dialog
                                dialogButton.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();
                            } else Toast.makeText(context, "Wrong Input Pattern", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        });
        adapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    private void write(String text, Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
        writeBtnClicked=0;
    }



    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }


    @Override
    protected void onNewIntent(Intent intent){
        if(writeBtnClicked==1) {
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                //write book barcode on its NFC tag
                try {
                    if (mytag == null) {
                        Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_SHORT).show();
                    } else {
                        if (field.getSelectedItemPosition() == 0) {
                            write("BK" + message.getText().toString(), mytag);
                            Toast.makeText(ctx, ctx.getString(R.string.ok_writing_book), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            message.setText("");
                        }
                        if (field.getSelectedItemPosition() == 1) {
                            write("SH" + message.getText().toString(), mytag);
                            Toast.makeText(ctx, ctx.getString(R.string.ok_writing_shelf), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            message.setText("");
                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                }
            }
        }
        else Toast.makeText(context,"Click Write Button First",Toast.LENGTH_SHORT).show();
    }

    //@Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }

    private void WriteModeOn(){
        writeMode = true;
        adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff(){
        writeMode = false;
        adapter.disableForegroundDispatch(this);
    }
}
