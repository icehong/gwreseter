package app.icehong.gwreseter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity  {

    private Button postButton = null;
    private Spinner spinner = null;

    public String type;
    public String ip;
    public String username ;
    public String password ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postButton = (Button) findViewById(R.id.raw_tcp);
        postButton.setOnClickListener(mPostClickraw_tcp);
        spinner = (Spinner)findViewById(R.id.spinner);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.modem_list, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        SharedPreferences settings = getSharedPreferences("CONFIG", MODE_PRIVATE);
        type = settings.getString("TYPE","");
        ip = settings.getString("ip","192.168.1.1");
        username = settings.getString("USERNAME","telecomadmin");
        password = settings.getString("PASSWORD","");

        EditText ed_ip = (EditText) findViewById(R.id.ip);
        ed_ip.setText(ip);
        EditText ed_name = (EditText) findViewById(R.id.username);
        ed_name.setText(username);
        EditText ed_pass = (EditText) findViewById(R.id.password);
        ed_pass.setText(password);

        TextView desc = (TextView) findViewById(R.id.about_desc);
        desc.setText(Html.fromHtml( getResources().getString(R.string.about_desc)));

    }
    private OnClickListener mPostClickraw_tcp = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            EditText ed_ip = (EditText) findViewById(R.id.ip);
            ip = ed_ip.getText().toString();
            EditText ed_name = (EditText) findViewById(R.id.username);
            username = ed_name.getText().toString();
            EditText ed_pass = (EditText) findViewById(R.id.password);
            password = ed_pass.getText().toString();

            //获取SharedPreferences对象
            Context ctx = MainActivity.this;
            SharedPreferences sp = ctx.getSharedPreferences("CONFIG", MODE_PRIVATE);
            //存入数据
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("TYPE", type);
            editor.putString("IP", ip);
            editor.putString("USERNAME", username);
            editor.putString("PASSWORD", password);
            editor.apply();

            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket s =new Socket();
                        s.connect(new InetSocketAddress(ip, 80), 30000);
                        // outgoing stream redirect to socket
                        OutputStream out = s.getOutputStream();
                        // 注意第二个参数据为true将会自动flush，否则需要需要手动操作out.flush()
                        PrintWriter output = new PrintWriter(out, true);

                        output.write("POST /html/login.cgi?Username=" +
                                username +
                                "&Password=" +
                                Base64.encode(password.getBytes()) +
                                "&Language=1&RequestFile=html/content.asp HTTP/1.1\r\n" +
                                "Accept: text/html, application/xhtml+xml, */*\r\n" +
                                "Referer: http://"+ip+"/html/index2.asp\r\n" +
                                "Accept-Language: zh-CN\r\n" +
                                "Content-Type: application/x-www-form-urlencoded\r\n" +
                                "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko\r\n" +
                                "Accept-Encoding: gzip, deflate\r\n" +
                                "Host: " + ip +"\r\n" +
                                "Content-Length: 0\r\n" +
                                "DNT: 1\r\n" +
                                "Connection: Keep-Alive\r\n" +
                                "Cache-Control: no-cache\r\n" +
                                "Cookie: FirstMenu=Admin_0; SecondMenu=Admin_0_0; ThirdMenu=Admin_0_0_0\r\n" +
                                "\r\n" +
                                "\r\n");
                        output.flush();

                        BufferedReader input = new BufferedReader(new InputStreamReader(s
                                .getInputStream()));
                        // read line(s)

                        String message ;
                        String sessionid = "";
                        while((message = input.readLine()) != null)
                        {
                            if(message.startsWith("Set-cookie:sessionID="))
                            {
                                int index = message.indexOf("=");
                                sessionid = message.substring(index +1 ,index + 17 );
                            }
                        }
                        Log.d("Tcp Demo", "session =" + sessionid);
                        s.close();

                        Socket s1 =new Socket();
                        s1.connect(new InetSocketAddress(ip, 80), 30000);
                        // outgoing stream redirect to socket
                        out = s1.getOutputStream();
                        // 注意第二个参数据为true将会自动flush，否则需要需要手动操作out.flush()
                        output = new PrintWriter(out, true);

                        output.write("GET /html/management/reboot.cgi HTTP/1.1\r\n" +
                                "Accept: text/html, application/xhtml+xml, */*\r\n" +
                                "Referer: http://"+ip+"/html/management/reset.asp\r\n" +
                                "Accept-Language: zh-CN\r\n" +
                                "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko\r\n" +
                                "Accept-Encoding: gzip, deflate\r\n" +
                                "Host: "+ip+"\r\n" +
                                "DNT: 1\r\n" +
                                "Connection: Keep-Alive\r\n" +
                                "Cookie: FirstMenu=Admin_0; SecondMenu=Admin_0_0; ThirdMenu=Admin_0_0_0; sessionID=" +
                                sessionid + "\r\n\r\n\r\n");
                        output.flush();

                        input = new BufferedReader(new InputStreamReader(s1
                                .getInputStream()));

                        while((message = input.readLine()) != null)
                        {
                            if(message.startsWith("Connection:Close"))
                            {
                                Log.d("Tcp Demo", "message From Server:" + message);
                            }
                        }
                        s1.close();

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    };

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
