package ir.tokaterm.fruitequiz;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
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
        answerTextView=view.findViewById(R.id.answerTextView);

        questionNumberTextView.setText(getString(R.string.question,1,flagsInQuiz));


        for (LinearLayout row:guessesLinearLayouts){
            for(int column=0;column<row.getChildCount();column++){
                Button button= (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }
   return view;
    }

    private View.OnClickListener guessButtonListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Button guessButton=(Button)v;
            String guess= guessButton.getText().toString();
            String answer=getCountryName(correctAnswer);

            if(totalGuesses<flagsInQuiz){
                totalGuesses++;
                if(guess.equals(answer)){
                    ++correctAnswers;
                    answerTextView.setText(answer+"!");
                    answerTextView.setTextColor(ContextCompat.getColor(getActivity(),R.color.correct_answer));
                    disableButtons();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                        }
                    },1000);

                }

                else {

                    flagImageView.startAnimation(shakeAnimation);
                    answerTextView.setText(R.string.incorrect_answer);
                    answerTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.incorrect_answer));
                    disableButtons();
                    guessButton.setEnabled(false);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                        }
                    },1000);

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

        String nextImage=quizCountriesList.remove(0);
        correctAnswer=nextImage;
        answerTextView.setText("");
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
        int column=random.nextInt(2);
        LinearLayout randomRow=guessesLinearLayouts[row];
        String countryName=getCountryName(correctAnswer);
        ((Button)randomRow.getChildAt(column)).setText(countryName);



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

}
















