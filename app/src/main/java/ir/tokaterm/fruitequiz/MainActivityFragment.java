package ir.tokaterm.fruitequiz;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainActivityFragment extends Fragment {

    public static final int flagsInQuiz=10;
    private List<String> fileNameList;
    private List<String> quizCountriesList;
    private Set<String> regionSet;
    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;
    private Animation shakeAnimation;
    private LinearLayout quizLinearLayout;
    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private LinearLayout[] guessesLinearLayouts;
    private TextView answerTextView;
    public MediaPlayer correctsound;
    public MediaPlayer incorrectsound;
    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private int  iprogress;
    private Button nextBtn;
    private int rowAnswer;
    private int columnAnswer;



    public MainActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_main_activity, container, false);

        fileNameList=new ArrayList<>();
        quizCountriesList=new ArrayList<>();
        random=new SecureRandom();
        handler=new Handler();
        shakeAnimation= AnimationUtils.loadAnimation(getActivity(),R.anim.incorrect_shake);
        quizLinearLayout=view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView=view.findViewById(R.id.questionNumberTextView);
        flagImageView=view.findViewById(R.id.flagImageView);
        guessesLinearLayouts=new LinearLayout[4];
        guessesLinearLayouts[0]=view.findViewById(R.id.row1Linearlayout);
        guessesLinearLayouts[1]=view.findViewById(R.id.row2LinearLayout);
        guessesLinearLayouts[2]=view.findViewById(R.id.row3LinearLayout);
        guessesLinearLayouts[3]=view.findViewById(R.id.row4LinearLayout);

        questionNumberTextView.setText(getString(R.string.question,1,flagsInQuiz));

        correctsound=MediaPlayer.create(getContext(),R.raw.correctsound);
        incorrectsound=MediaPlayer.create(getContext(),R.raw.incorrectsound);

        progressBar=view.findViewById(R.id.progressBarTime);
        nextBtn=view.findViewById(R.id.nextBtn);


        for (LinearLayout row:guessesLinearLayouts){
            for(int column=0;column<row.getChildCount();column++){
                Button button= (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }
   return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        countDownTimer.cancel(); 

    }

    private View.OnClickListener guessButtonListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Button guessButton=(Button)v;
            String guess= guessButton.getText().toString();
            String answer=getCountryName(correctAnswer);
            countDownTimer.cancel();
            if(totalGuesses<flagsInQuiz){
                totalGuesses++;
                if(guess.equals(answer)){
                    correctsound.start();
                    ++correctAnswers;
                    disableButtons();
                    guessButton.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.correctbuttonstyle));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                            guessButton.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.defaultbuttonstyle));
                        }
                    },2000);


                }

                else {
                     incorrectsound.start();
                    flagImageView.startAnimation(shakeAnimation);
                    disableButtons();
                    findBtn(true);
                    guessButton.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.incorrectbuttonstyle));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                            findBtn(false);
                            guessButton.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.defaultbuttonstyle));
                        }
                    },2000);


                }

            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getString(R.string.results, correctAnswers, (10*correctAnswers)));
                builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
            }


        }
    };





private void disableButtons(){

    for (int row=0;row<guessRows;row++){

        LinearLayout guessRows=guessesLinearLayouts[row];
        for(int column=0;column<guessRows.getChildCount();column++) {
            guessRows.getChildAt(column).setEnabled(false);
        }
        }

}


    public void resetQuiz(){

        AssetManager assetm=getContext().getAssets();
        fileNameList.clear();
      try {
        for(String regin:regionSet){
            String[] items=assetm.list(regin);
            for(String item:items){
                fileNameList.add(item.replace(".png",""));
            }
        }
      }catch (IOException e){
          Log.e("flagQuiz","Error loading image file name",e);
      }

      correctAnswers=0;
      totalGuesses=1;
      quizCountriesList.clear();
      int flagCounter=1;
      int numberOfFlags=fileNameList.size();
        while (flagCounter<=flagsInQuiz){
            int randomIndex=random.nextInt(numberOfFlags);
            String fileName=fileNameList.get(randomIndex);
            if(!quizCountriesList.contains(fileName))
            {
                quizCountriesList.add(fileName);
                ++flagCounter;
            }
        }
       loadNextFlag();
    }

    private void loadNextFlag(){
        nextBtn.setVisibility(View.GONE);
        String nextImage=quizCountriesList.remove(0);
        correctAnswer=nextImage;
        questionNumberTextView.setText(getString(R.string.question,(totalGuesses),flagsInQuiz));
        String region=nextImage.substring(0,nextImage.indexOf('-'));
        AssetManager assets=getActivity().getAssets();
        try{
            InputStream stream=assets.open(region+"/"+nextImage+".png");
            Drawable flag=Drawable.createFromStream(stream,nextImage);
            flagImageView.setImageDrawable(flag);
            animate(false);
        }
        catch (IOException exception){
            Log.e("FlagQuiz","Error loading "+nextImage,exception);
        }

        Collections.shuffle(fileNameList);
        int correct=fileNameList.indexOf(correctAnswer);
        String s=fileNameList.remove(correct);
        fileNameList.add(s);

        for (int row=0;row<guessRows;row++){

            for(int column=0;column<guessesLinearLayouts[row].getChildCount();column++){

                Button newGuessButton=(Button)guessesLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);
                String fileName=fileNameList.get((row*2)+column);
                newGuessButton.setText(getCountryName(fileName));
            }
        }
        int row=random.nextInt(guessRows);
        rowAnswer=row;
        int column=random.nextInt(2);
        columnAnswer=column;
        LinearLayout randomRow=guessesLinearLayouts[row];
        String countryName=getCountryName(correctAnswer);
        ((Button)randomRow.getChildAt(column)).setText(countryName);
       progressTimer();


    }

    private String getCountryName(String name){

        return name.substring(name.indexOf('-')+1).replace('_',' ');
    }



    private void animate(boolean animateOut){

        if(totalGuesses==1)
            return;
        int centerX=(quizLinearLayout.getLeft()+quizLinearLayout.getRight())/2;
        int centerY=(quizLinearLayout.getTop()+quizLinearLayout.getBottom()/2);
        int radius=Math.max(quizLinearLayout.getWidth(),quizLinearLayout.getHeight());
        Animator animator;

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            if(animateOut){
                animator= ViewAnimationUtils.createCircularReveal(quizLinearLayout,centerX,centerY,radius,0);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        loadNextFlag();
                    }
                });

            }else {
                animator=ViewAnimationUtils.createCircularReveal(quizLinearLayout,centerX,centerY,0,radius);
            }
            animator.setDuration(500);
            animator.start();
        }else {

            if(animateOut)
                loadNextFlag();
        }
    }


    public void updateGuessRows(SharedPreferences sharedPreferences){

        String choices=sharedPreferences.getString(MainActivity.CHOICES,null);
        guessRows=Integer.parseInt(choices)/2;

        for (LinearLayout layout:guessesLinearLayouts){
            layout.setVisibility(View.GONE);
        }
        for (int row=0;row<guessRows;row++){

            guessesLinearLayouts[row].setVisibility(View.VISIBLE);
        }
    }


    public void updateRegions(SharedPreferences sharedPreferences){

     regionSet=sharedPreferences.getStringSet(MainActivity.REGIONS,null);
    }

    public void progressTimer(){

    iprogress=1;
    progressBar.setProgress(100);
    countDownTimer=new CountDownTimer(10000,100) {
        @Override
        public void onTick(long millisUntilFinished) {

            iprogress++;
            progressBar.setProgress((int)(iprogress*(-1))+100);

        }

        @Override
        public void onFinish()
        {
            progressBar.setProgress(0);
            totalGuesses++;
            disableButtons();
                findBtn(true);
                nextBtn.setVisibility(View.VISIBLE);
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        animate(true);
                        findBtn(false);
                    }
                });
        }
    };
    countDownTimer.start();

    }


    private void findBtn(Boolean check){

        LinearLayout randomRow = guessesLinearLayouts[rowAnswer];
       if(check) {
           ((Button) randomRow.getChildAt(columnAnswer)).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.correctbuttonstyle));
       }else {
           ((Button) randomRow.getChildAt(columnAnswer)).setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.defaultbuttonstyle));
       }
    }

}
















