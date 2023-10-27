package com.karatekids.wikipediarace;

import static com.karatekids.wikipediarace.InGameActivity.onClickStart;
import static com.karatekids.wikipediarace.MainActivity.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LobbyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        if(b.getString("game_mode").equals("multi")) {
            setContentView(R.layout.activity_multi_player_lobby);
        }
        else {
            setContentView(R.layout.activity_single_player_lobby);
        }
        findViewById(R.id.start_game_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Networker.requestGame(true, LobbyActivity.this);
                Intent startGameIntent = new Intent(LobbyActivity.this, InGameActivity.class)
                        .putExtra("start_page","Taco")
                        .putExtra("end_page","Mexico")
                        .putExtra("start_url","https://en.m.wikipedia.org/wiki/Taco")
                        .putExtra("end_url","https://en.m.wikipedia.org/wiki/Mexico");
                onClickStart(v);
                startActivity(startGameIntent);
            }
        });
        //TODO: remove button for starting game and automatically start game when all players have joined
        //----- send request to join game----
//        if(b.getString("game_mode").equals("multi")) {
//            Networker.requestGame(true, LobbyActivity.this);
//        }
//        else{
//            Networker.requestGame(false, LobbyActivity.this);
//        }


        //---- receive request to join game -----

        //---- receive response that no other players are waiting in lobby ----

        //to hide loading progress bar:
        // findViewById(R.id.loading_pb).setVisibility(View.GONE);
    }

    public void matchFound(String data){

    //Json string with list of players, ready to start game and intent
        Intent startGameIntent = new Intent(LobbyActivity.this, InGameActivity.class)
                .putExtra("data", data)
                .putExtra("start_page","Taco")
                .putExtra("end_page","Mexico")
                .putExtra("start_url","https://en.m.wikipedia.org/wiki/Taco")
                .putExtra("end_url","https://en.m.wikipedia.org/wiki/Mexico");

        //TODO: add toasts for start page end page and list of opponents

        //startActivity(startGameIntent);
    }

}
