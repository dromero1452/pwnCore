package com.anwarelmakrahy.pwncore.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import com.anwarelmakrahy.pwncore.MainService;
import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.StaticsClass;
import com.anwarelmakrahy.pwncore.console.ConsoleSession;
import com.anwarelmakrahy.pwncore.console.ConsoleSession.ConsoleSessionParams;
import com.anwarelmakrahy.pwncore.console.utils.PortScanner;
import com.anwarelmakrahy.pwncore.structures.TargetItem;
import com.anwarelmakrahy.pwncore.structures.TargetsListAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AttackWizardActivity extends Activity {
	private ProgressBar progress;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {   	
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_attackwizard);
        
        prefs = this.getSharedPreferences("com.anwarelmakrahy.pwncore", Context.MODE_PRIVATE);
        
		mTargetHostListView = (ListView)findViewById(R.id.targetsListView1);	
		mTargetHostListAdapter = new TargetsListAdapter(this, MainService.mTargetHostList);
		mTargetHostListView.setAdapter(mTargetHostListAdapter);
		mTargetHostListView.setEmptyView(findViewById(R.id.consolePrompt));
		registerForContextMenu(mTargetHostListView);
		((TextView)findViewById(R.id.targetsCount)).setText("Current Targets: " + MainService.mTargetHostList.size());
		
		progress = (ProgressBar)findViewById(R.id.progress2);
		setProgressBar(false);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attackwizard, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	    switch (item.getItemId()) {    	
	        
	    case R.id.mnuNmapComprehensive:
	    	showNmapHostDlg(new String[] {	"-sS","-sU","-T4","-A","-v","-PE","-PP","-PS80,443","-PA3389",
	    									"-PU40125","-PY","-g","53","--script", "default or (discovery and safe)","-oX","-"});
	    	return true;
	    case R.id.mnuNmapIntense:
	    	showNmapHostDlg(new String[] {"-T4","-A","-v","-oX","-"});
	    	return true;
	    case R.id.mnuNmapIntenseUDP:
	    	showNmapHostDlg(new String[] {"-sS","-sU","-T4","-A","-v","-oX","-"});
	    	return true;
	    case R.id.mnuNmapIntenseNoPing:
	    	showNmapHostDlg(new String[] {"-T4","-A","-v","-Pn","-oX","-"});
	    	return true;
	    case R.id.mnuNmapIntenseTCP:
	    	showNmapHostDlg(new String[] {"-p","1-65535","-T4","-A","-v","-oX","-"});
	    	return true;
	    case R.id.mnuNmapPing:
	    	showNmapHostDlg(new String[] {"-sn","-v","-oX","-"});
	    	return true;
	    case R.id.mnuNmapQuick:
	    	showNmapHostDlg(new String[] {"-T4","-F","-v","-oX","-"});
	    	return true;
	    case R.id.mnuNmapQuickOS:
	    	showNmapHostDlg(new String[] {"-T4","-F","-O","-v","-oX","-"});
	    	return true; 
	    	
	    case R.id.mnuHostsManualFeed:
	    	showManualHostDlg();
	    	return true;  
	    case R.id.mnuImportHostsFile:
	    	showFileChooser();
	    	return true;
	    case R.id.mnuClearHosts:
	    	MainService.mTargetHostList.clear();
	    	mTargetHostListAdapter.notifyDataSetChanged();
	    	((TextView)findViewById(R.id.targetsCount)).setText("Current Targets: 0");
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showNmapHostDlg(final String[] args) {
    	final EditText input = new EditText(this);
    	input.setSingleLine(true);
    	AlertDialog.Builder alert = new AlertDialog.Builder(this)
    	.setMessage("Enter scan range (eg. 10.0.0.0/24)")
    	.setView(input)
    	.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    	  String host = input.getText().toString();
	    	  
	    	  if (StaticsClass.validateIPAddress(host, true))
	    		  startNmapScan(args, host);
	    	  else 
	    		  showNmapHostDlg(args);
	    	}
    	})
    	.setNegativeButton("Cancel", null);
    	alert.show();
    }
	
	private void startNmapScan(String[] args, String host) {

		Intent tmpIntent = new Intent();
		tmpIntent.setAction(StaticsClass.PWNCORE_CONSOLE_CREATE);
		tmpIntent.putExtra(StaticsClass.PWNCORE_NMAP_SCAN_ARGS, args);
		tmpIntent.putExtra(StaticsClass.PWNCORE_NMAP_SCAN_HOST, host);
		tmpIntent.putExtra(StaticsClass.PWNCORE_CONSOLE_TYPE, StaticsClass.PWNCORE_CONSOLE_TYPE_NMAP);
		sendBroadcast(tmpIntent);
		
		Toast.makeText(getApplicationContext(), 
				"Nmap scan started.", 
				Toast.LENGTH_LONG).show();
		
		setProgressBar(true);
    }
	
	private ListView mTargetHostListView;
	private TargetsListAdapter mTargetHostListAdapter;
	private boolean conStatusReceiverRegistered = false;
	private boolean isConnected = true;
	private SharedPreferences prefs;
	
	private void addHostToTargetList(TargetItem item) {	
		for (int i=0; i<MainService.mTargetHostList.size(); i++)
			if (MainService.mTargetHostList.get(i).getHost().equals(item.getHost())) {
				return;
			}
	    	
    	MainService.mTargetHostList.add(0,item);
    	mTargetHostListAdapter.notifyDataSetChanged();
		scan(item.getHost());
    	((TextView)findViewById(R.id.targetsCount)).setText("Current Targets: " + MainService.mTargetHostList.size());
    }
	 
	private void removeHostFromTargetList(int pos) {	
		MainService.mTargetHostList.remove(pos);
    	mTargetHostListAdapter.notifyDataSetChanged();
    	((TextView)findViewById(R.id.targetsCount)).setText("Current Targets: " + MainService.mTargetHostList.size());
    }
	
	private void scan(final String host) {
		new Thread(new Runnable() {

			@Override
			public void run() {
			    //ConsoleSessionParams scannerParams = new ConsoleSessionParams();
			    //scannerParams.setAcivity(AttackWizardActivity.this);
			    //scannerParams.setCmdViewId(R.id.consoleRead);
			    //scannerParams.setPromptViewId(R.id.consolePrompt);
				    
			    PortScanner scanner = new PortScanner(getApplicationContext()/*, scannerParams*/);
			    MainService.sessionMgr.getNewConsole(scanner);
			    if (scanner != null)
			    	scanner.scan(host);
			}
			
		}).start();

	}
	
	private void showManualHostDlg() {
       	final EditText input = new EditText(this);
       	input.setSingleLine(false);
    	AlertDialog.Builder alert = new AlertDialog.Builder(this)
    	.setMessage("Enter one host/line")
    	.setView(input)
    	.setNegativeButton("Cancel", null)
    	.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			String[] hosts = input.getText().toString().split("\n");
	    	  
    			for (int i=0; i<hosts.length; i++) {
    				if (StaticsClass.validateIPAddress(hosts[i], false)) {
    					addHostToTargetList(new TargetItem(hosts[i]));				
    				}
    			}
    		}
    	});
    	alert.show();
    }
	
	private static final int FILE_SELECT_CODE = 100;
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();           
				try {
					String path = StaticsClass.getPath(this, uri);
					File file = new File(path);
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;
				    while ((line = br.readLine()) != null) {
	    				if (StaticsClass.validateIPAddress(line, false)) {
	    					addHostToTargetList(new TargetItem(line));				
	    				}
				    }				    
				    br.close();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    
    private static String[] target_contextmenu_titles = { "Change OS", "Remove Host" };
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == mTargetHostListView) {
            menu.add(0, v.getId(), 0, target_contextmenu_titles[0]);
        	menu.add(0, v.getId(), 0, target_contextmenu_titles[1]);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getTitle().equals(target_contextmenu_titles[1])) {
        	removeHostFromTargetList(info.position);
        }
        else if (item.getTitle().equals(target_contextmenu_titles[0])) {  	
        	AlertDialog builder = new AlertDialog.Builder(this)
            .setSingleChoiceItems(TargetsListAdapter.osTitles, -1, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int item) {
	            	dialog.dismiss();
	        		MainService.mTargetHostList.get(info.position).setOS(TargetsListAdapter.osTitles[item]);
	        		mTargetHostListAdapter.notifyDataSetChanged();	
                }
            })
            .create();
            builder.show();     	
        }
        
        return super.onContextItemSelected(item);   
    }
    
    private void setProgressBar(boolean state) {
    	if (state) {
			progress.setIndeterminate(true);
			progress.setVisibility(View.VISIBLE);
    	}
    	else {
			progress.setIndeterminate(false);
			progress.setVisibility(View.INVISIBLE);
    	}
    }
     
	@Override
	public void onResume() {
		if (!conStatusReceiverRegistered) {
			IntentFilter filter = new IntentFilter();	
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_FAILED);
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_TIMEOUT);
			filter.addAction(StaticsClass.PWNCORE_CONNECTION_LOST);
			registerReceiver(conStatusReceiver, filter);
			conStatusReceiverRegistered = true;
		}
			
		isConnected = prefs.getBoolean("isConnected", false);
		super.onResume();
	}
    
	@Override
	public void onDestroy() {		
		if (conStatusReceiverRegistered ) {
			unregisterReceiver(conStatusReceiver);
			conStatusReceiverRegistered = false;
		}
		super.onDestroy();
	}
    
    public BroadcastReceiver conStatusReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		
    		if (action == StaticsClass.PWNCORE_CONNECTION_TIMEOUT) {		
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionTimeout: Please check that server is running", 
    					Toast.LENGTH_SHORT).show();
    		}  				
    		else if (action == StaticsClass.PWNCORE_CONNECTION_FAILED) {
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionFailed: " + intent.getStringExtra("error"), 
    					Toast.LENGTH_SHORT).show();    	
    		}		
    		else if (action == StaticsClass.PWNCORE_CONNECTION_LOST) {
    			prefs.edit().putBoolean("isConnected", false).commit();
    			Toast.makeText(getApplicationContext(), 
    					"ConnectionLost: Please check your network settings", 
    					Toast.LENGTH_SHORT).show();
    			finish();
    		}
    	}
    };
    
    public void launchAttack(View v) {
    	isConnected = prefs.getBoolean("isConnected", false);
    	
    	if (MainService.mTargetHostList.size() == 0)
			Toast.makeText(getApplicationContext(), "You have no targets", Toast.LENGTH_SHORT).show();
    	
    	else if (isConnected) {
    		startActivity(new Intent(getApplicationContext(), AttackHallActivity.class));
    		finish();
    	}
    	else
			Toast.makeText(getApplicationContext(), 
					"NoConnection: Please check your connection", 
					Toast.LENGTH_SHORT).show();
    }
    
}
