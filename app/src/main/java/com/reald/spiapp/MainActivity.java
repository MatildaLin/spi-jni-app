package com.reald.spiapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = MainActivity.class.getSimpleName();
    private Context mContext;

    private TextView mFilePathTextView;
    private TextView mResultTextView;
    private Button mFileButton;
    private Button mSendButton;
    private Spinner mModeSpinner;

    private final String WRITE_CMD = "W ";
    private final String READ_CMD = "R ";
    private final String PAUSE_CMD = "P ";

    private List<String> mModeList = new ArrayList<String>();
    private ArrayAdapter<String> mModeListAdapter;

    private FileChooser.FileSelectedListener mFileListener;

    private StringBuilder mSbResult = new StringBuilder("");

    public int mFD = 0;

    static {
        System.loadLibrary("BBBAndroidHAL");
    }

    public native String stringFromJNI();

    public native int spiOpen(int bus, int device, int speed, int mode, int bpw);

    public native byte spiReadByte(int spiFD, int regAdd);

    public native int spiWriteRegByte(int spiFD, int regAdd, byte data);

    public native int spiPause(int spiFD, int data);

    public native void spiClose(int spiFD);

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mContext = this.getApplicationContext();

        mFilePathTextView = (TextView) findViewById(R.id.textViewFilePath);
        mResultTextView = (TextView) findViewById(R.id.textViewResult);

        mFileButton = (Button) findViewById(R.id.buttonSelectFile);
        mFileButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileChooser fc = new FileChooser(MainActivity.this);
                fc.setFileListener(mFileListener);
                fc.showDialog();
            }
        });

        mFileListener = new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                mFilePathTextView.setText(file.getAbsolutePath());

                // clean the mode list
                mModeList.clear();

                // read file, and parse file
                processSettingsFile(file);

                updateModeList();
            }
        };

        mModeSpinner = (Spinner) findViewById(R.id.spinnerMode);

        mSendButton = (Button) findViewById(R.id.buttonSend);
        mSendButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand(mModeSpinner.getSelectedItem().toString());
            }
        });

        updateModeList();

        openSPI();
    }

    public void processSettingsFile(File file) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));

            String readingLine;
            while ((readingLine = br.readLine()) != null) {

                if (readingLine.startsWith(WRITE_CMD) || readingLine.startsWith(READ_CMD)
                        || readingLine.startsWith(PAUSE_CMD)) {
                    // add to spinner
                    mModeList.add(readingLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateModeList() {
        mModeList.add(0, getString(R.string.mode_select));
        mModeListAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, mModeList);
        mModeSpinner.setAdapter(mModeListAdapter);
    }

    public void sendCommand(String selectedCmd) {
        Log.d(TAG, "sendCommand: " + selectedCmd);
        if (selectedCmd.equals(getString(R.string.mode_select))) {
            return;

        } else if (selectedCmd.startsWith(WRITE_CMD)) {
            String[] cmd = selectedCmd.split(" ");
            if (cmd.length == 3) {
                writeRegister(cmd[1], cmd[2]);
            } else {
                log("write command error");
            }

        } else if (selectedCmd.startsWith(READ_CMD)) {
            String[] cmd = selectedCmd.split(" ");
            if (cmd.length == 2) {
                readRegister(cmd[1], "");
            } else if (cmd.length == 3) {
                readRegister(cmd[1], cmd[2]);
            } else {
                log("read command error");
            }

        } else if (selectedCmd.startsWith(PAUSE_CMD)) {
            String[] cmd = selectedCmd.split(" ");
            if (cmd.length == 2) {
                pause(cmd[1]);
            } else {
                log("pause command error");
            }
        }
        Toast.makeText(mContext, stringFromJNI(), Toast.LENGTH_SHORT).show();
    }

    public void openSPI() {
        mFD = spiOpen(1, 0, 10000, 3, 8);
        if (mFD < 0) {
            log("SPI JNI: failed to open spi device.");
            log("GG failed!!!! hahahahaha");
            //mSendButton.setEnabled(false); //retry?
        } else {
            log("SPI JNI: spi device is open. fd= " + mFD);
        }
    }

    public void writeRegister(String regName, String regValue) {
        try {
            int regAdd = Integer.parseInt(regName.replace("0x", "")) & 0xff;
            long data = Long.parseLong(regValue.replace("0x", "")) & 0xffffffffL;

            // call JNI to write the reg
            int res = spiWriteRegByte(mFD, regAdd, (byte) data);
            if (res < 0) {
                log("SPI JNI: failed to write reg.");
            } else {
                log("SPI JNI: spi device has written. res= " + res);
            }

        } catch (NumberFormatException e) {
            log("write command: the format of the reg name or value is wrong");
        }
    }

    public void readRegister(String regName, String expValue) {
        try {
            int regAdd = Integer.parseInt(regName.replace("0x", "")) & 0xff;

            // call JNI to read the reg
            byte res = spiReadByte(mFD, regAdd); //parse again?
            int signedRes = res & 0xff;
            if (signedRes < 0) {
                log("SPI JNI: failed to read reg.");
            } else {
                log("SPI JNI: reg value= " + res);

                if (!expValue.isEmpty()) {
                    // compare the reg with expectation
                    if (signedRes == Integer.parseInt(expValue.replace("0x", ""))) {
                        log("SPI JNI: the same as the expectation value");
                        Toast.makeText(MainActivity.this, "the same as the expectation value", Toast.LENGTH_LONG).show();
                    } else {
                        log("SPI JNI: return value doesn't match the expectation value");
                    }
                }
            }
        } catch (NumberFormatException e) {
            log("read command: the format of the reg number or value is wrong");
        }
    }

    public void pause(String timeValue) {
        try {
            int time = Integer.parseInt(timeValue);

            // call JNI to pause
            spiPause(mFD, time);

        } catch (NumberFormatException e) {
            log("pause command: the format of the time value is wrong");
        }
    }

    public void log(String log) {
        System.out.println("[SPI app] " + log);
        mSbResult.append(mSbResult.toString().isEmpty() ? log : "\n" + log);
        mResultTextView.setText(mSbResult.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spiClose(mFD);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_about) {
            AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
            dBuilder.setTitle(getString(R.string.about));
            dBuilder.setMessage(getString(R.string.version));
            dBuilder.show();
            return true;

        } else if (id == R.id.menu_exit) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
