package com.karthikrk.codesprintapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView tw1,tw2,tw3,tw4,tw6;
    private Button btn1, btn2;
    private TrieNode tNode;
    private Random rand = new Random();
    private static final String COMPUTER_TURN="Computer is playing";
    private static final String USER_TURN="User is playing";
    private static final String USER_WINS="User won!!!";
    private static final String COMPUTER_WINS="Computer won!!!";
    private boolean userPlays=false;
    private Character[] alphabets = new Character[26];
    private int turn;
    private int turnlimit;
    private String computerWord;
    private static String computerBuild;
    private boolean spoofed = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for(int i=0; i<26;i++)
            alphabets[i] = (char)(i+97);
        AssetManager assetManager = getAssets();
        try{
            InputStream iStream = assetManager.open("words.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            tNode = new TrieNode();
            String line = null;
            while((line=br.readLine())!=null){
                String word = line.trim().toLowerCase();
                tNode.add(word);
            }
        }
        catch(IOException e){
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        onStart(null);
    }

    /**
     * Handler for the RESET button
     * @param view
     * @return
     */
    public boolean onStart(View view){
        turn=1;
        int alpha = rand.nextInt(26);
        turnlimit = rand.nextInt(5)+1;
        String c = Character.toString((char) (97+alpha));
        tw1 = (TextView) findViewById(R.id.textView);
        tw2 = (TextView) findViewById(R.id.textView2);
        tw3 = (TextView) findViewById(R.id.textView3);
        tw6 = (TextView) findViewById(R.id.textView6);
        tw1.setText(c);
        tw2.setText(c);
        tw3.setText(USER_TURN);
        tw6.setText("HEY! You can just guess "+turnlimit+" more letters!");
        userTurn();
        return true;
    }

    /**
     * Method for the user turn. Based on the turn, the user enters one character and then moves forward to the entire word.
     */

    public void userTurn(){
        //tw1.setEnabled(true);
        tw4 = (TextView) findViewById(R.id.textView4);
        tw3.setText(USER_TURN);

        if(turnlimit==0)
        {
            tw4.setText("Please enter the word you thought of. Press DONE to continue");
        }
        //turn=1;
        if(!(turn%2==0)&&turnlimit>0){

            tw4.setText("Please enter one character. Press DONE to continue");
            turn++;

        }
        else{
            tw4.setText("Please enter the word you thought of. Press DONE to continue");
        }
//        tw2.setEnabled(false);
//        tw3.setEnabled(false);
//        tw4.setEnabled(false);
    }

    /**
     * Method for the computer turn. This handles choosing the word and then updating the text field
     * @param s
     *          : Input prefix taken from the user's textbox
     */
    public void computerTurn(String s) {
        //tw2.setEnabled(true);
        tw3.setText(COMPUTER_TURN);
        if(turn==2)
        computerBuild = s;
        else
        computerBuild = computerBuild + s.charAt(s.length()-1);
        //int turn=1;
        if(turn%2==0&&turnlimit>0) {

            tw4.setText("Computer enters a character!!");
            computerWord= guessWord(computerBuild);
            if(computerWord==null) {
                spoofed = true;
                computerGiveUp();
            }
            String al = tw2.getText().toString();
            al = al + Character.toString(computerWord.charAt(s.length()-1));
            tw2.setText(al);
            computerBuild = computerWord.substring(0,s.length());
            turn++;
            userTurn();
        }
        else{
            tw4.setText("Computer has entered its word!!");
            tw2.setText(computerWord);
            findWinner();
        }
        turnlimit--;
        if(turnlimit>0) {
            tw6.setText("Feel free to guess " + (turnlimit) + " more letters!");
        }
        else {
            tw6.setText("You don't have letters to guess anymore!");
            tw4.setText("Please enter the word you thought of. Press DONE to continue");
        }

    }

    /**
     * Utility method to find the winner after the last turn. The method ends the game. RESET is pressed to make the game come
     * back to the start state.
     */
    public void findWinner() {
        String userWord = tw1.getText().toString();
        String compWord = tw2.getText().toString();
        if(tNode.isWord(userWord)) {
            if (userWord.length() > compWord.length()) {
                tw3.setText(USER_WINS);
            }
            else if(userWord.length() == compWord.length()){
                tw3.setText("Tie!!");
            }
            else {
                tw3.setText(COMPUTER_WINS);
            }
        }
        else
        tw3.setText(COMPUTER_WINS);
        tw4.setText("Press RESET to start a new game!!");
        if(spoofed)
            tw6.setText("You cannot spoof and enter the wrong word mate!");
    }

    /**
     * Handler for the DONE buttons
     */
    public void onClickDone(View view){
        computerTurn(tw1.getText().toString());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        char x = (char) keyEvent.getUnicodeChar();
        if(x>='a' && x<='z') {
            String s =  tw1.getText().toString();
            s=s+Character.toString(x);
            tw1.setText(s);
            //computerTurn(s);
        }
        else {
            return super.onKeyUp(keyCode,keyEvent);
        }
        return true;
    }

    /**
     * Method guessWord which is used by the computer to compute the longest word with the start alphabet. This is
     * dynamically modified as the user fills the text box with his word.
     *  Will have to fix spoofed computer turn yet as of October 29, 2016
     *
     *
     *
     * @param start
     *              : Prefix entered by the user
     * @return
     *              : Returns the longest word possible by excluding the prefix chosen by the user.
     */
    protected String guessWord(String start)
    {
        Character notToUse = start.charAt(start.length()-1);
        String toBuild = start.substring(0,start.length()-1);
        String longWord = null;
        PriorityQueue<Integer> pq = new PriorityQueue<Integer>(26, Collections.reverseOrder());

        HashMap<Integer, ArrayList<String>> wordMap = new HashMap<>();

        for(Character buildCharacter: alphabets)
        {
            if(buildCharacter!=notToUse)
            {
                longWord = tNode.getLongestWordStartingWith(toBuild + buildCharacter);

                if(longWord!=null) {
                    if (!wordMap.containsKey(longWord.length())) {
                        pq.add(longWord.length());
                        ArrayList<String> sameLengthWords = new ArrayList<>();
                        sameLengthWords.add(longWord);
                        wordMap.put(longWord.length(), sameLengthWords);

                    } else {
                        wordMap.get(longWord.length()).add(longWord);

                    }
                }
                else{
                    spoofed = true;
                    computerGiveUp();

                }

            }
        }

        String chosen = wordMap.get(pq.peek()).get(0);
        return chosen;
    }

    public void computerGiveUp()
    {
        tw6.setText("You have successfully spoofed Wordlete!");
        tw4.setText("Please enter the word you thought of. Press DONE to continue");
        findWinner();

    }
}
