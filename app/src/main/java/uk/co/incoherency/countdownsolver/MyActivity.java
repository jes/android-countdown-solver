package uk.co.incoherency.countdownsolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class MyActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    public static String DICTIONARY_JSON;
    public static JSONObject dictionary;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        if (dictionary == null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open("dictionary.json")));

                // do reading, usually loop until end of file reading
                DICTIONARY_JSON = "";
                String mLine = reader.readLine();
                while (mLine != null) {
                    //process line
                    DICTIONARY_JSON += mLine;
                    mLine = reader.readLine();
                }
            } catch (IOException e) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        //log the exception
                    }
                }
            }

            try {
                dictionary = new JSONObject(DICTIONARY_JSON);
            } catch (JSONException e) {
            }
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return LettersFragment.newInstance();
            else
                return NumbersFragment.newInstance();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Letters".toUpperCase(l);
                case 1:
                    return "Numbers".toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LettersFragment extends Fragment {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static LettersFragment newInstance() {
            LettersFragment fragment = new LettersFragment();
            return fragment;
        }

        public LettersFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_letters, container, false);

            final EditText edit = (EditText)rootView.findViewById(R.id.editText);
            final TextView resultsView = (TextView)rootView.findViewById(R.id.lettersResults);
            edit.addTextChangedListener(new TextWatcher() {
                ArrayList<String> words;
                @Override
                public void onTextChanged(CharSequence s, int a, int b, int c) {
                    resultsView.setText(solve(s));
                }

                private String solve(CharSequence s) {
                    boolean[] used_letter = new boolean[s.length()];
                    words = new ArrayList<String>();
                    recurse_solve_letters(s, MyActivity.dictionary, used_letter, "");
                    Collections.sort(words, new LengthComparator());
                    String results = "";
                    for (int i = 0; i < words.size(); i++)
                        results += words.get(i) + "\n";
                    return results;
                }

                private void recurse_solve_letters(CharSequence s, JSONObject dictionary, boolean[] used_letter, String answer) {
                    if (dictionary.has("0"))
                        words.add(answer);

                    if (answer.length() == s.length())
                        return;

                    for (int i = 0; i < s.length(); i++) {
                        if (used_letter[i])
                            continue;

                        boolean doneletter = false;
                        for (int j = 0; j < i; j++)
                            if (!used_letter[j] && s.charAt(j) == s.charAt(i))
                                doneletter = true;
                        if (doneletter)
                            continue;

                        char c = s.charAt(i);
                        if (dictionary.has(Character.toString(c))) {
                            used_letter[i] = true;
                            try {
                                recurse_solve_letters(s, (JSONObject)dictionary.get(Character.toString(c)), used_letter, answer + c);
                            } catch (JSONException e) { }
                            used_letter[i] = false;
                        }
                    }
                }

                class LengthComparator implements java.util.Comparator<String> {

                    public LengthComparator() {
                        super();
                    }

                    public int compare(String s1, String s2) {
                        return s2.length() - s1.length();
                    }
                }

                @Override
                public void afterTextChanged(Editable e) {}
                @Override
                public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            });

            Button reset = (Button)rootView.findViewById(R.id.resetLetters);
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    edit.setText("");
                }
            });

            return rootView;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class NumbersFragment extends Fragment {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static NumbersFragment newInstance() {
            NumbersFragment fragment = new NumbersFragment();
            return fragment;
        }

        public NumbersFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_numbers, container, false);

            EditText target = (EditText)rootView.findViewById(R.id.target);
            final TextView numbersResults = (TextView)rootView.findViewById(R.id.numbersResults);
            target.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    numbersResults.setText(solve());
                    return false;
                }

                private NumberTree bestresult;
                private double bestvalsums;

                private String solve() {
                    int target;
                    NumberTree[] number;

                    try {
                        target = Integer.parseInt(((EditText) rootView.findViewById(R.id.target)).getText().toString());
                        number = new NumberTree[6];
                        int[] id = {R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5, R.id.num6};
                        for (int i = 0; i < 6; i++) {
                            number[i] = new NumberTree(Integer.parseInt(((EditText) rootView.findViewById(id[i])).getText().toString()));
                        }
                    } catch (NumberFormatException e) {
                        return "Invalid number format";
                    }

                    Arrays.sort(number);

                    bestresult = number[0];
                    for (int i = 1; i < 6; i++) {
                        if (Math.abs(number[i].value - target) < Math.abs(bestresult.value - target))
                            bestresult = number[i];
                    }
                    if (bestresult.value == target)
                        return target + " = " + target;

                    solve_numbers(number, target);

                    return _stringify_result(_serialise_result(_tidyup_result(bestresult)), target);
                }

                private void solve_numbers(NumberTree[] number, int target) {
                    boolean[] was_generated = new boolean[6];
                    for (int i = 0; i < 6; i++)
                        was_generated[i] = false;

                    _solve_numbers(number, 0, was_generated, target, 6, 0);
                }

                private NumberTree _tidyup_result(NumberTree r) {
                    /*var swappable = {
                        "*": true, "+": true
                    };*/

                    if (r.sources == null || r.sources.length < 2)
                        return r;

                    for (int i = 0; i < r.sources.length; i++) {
                        NumberTree child = r.sources[i];

                        child = _tidyup_result(child);

                        if (child.op == r.op && (r.op == '*' || r.op == '+')) {
                            r.deleteSource(i--);
                            for (int j = 0; j < child.sources.length; j++) {
                                r.addSource(child.sources[j]);
                            }
                        } else {
                            r.sources[i] = child;
                        }
                    }

                    if (r.op == '?' || r.op == '_') {
                        r.op = (r.op == '?') ? '/' : '-';
                        NumberTree j = r.sources[0];
                        r.sources[0] = r.sources[1];
                        r.sources[1] = j;
                    } else if (r.op == '*' || r.op == '+') {
                        Arrays.sort(r.sources, new Comparator<NumberTree>() {
                            public int compare(NumberTree a, NumberTree b) {
                                return (int)(b.value - a.value);
                            }
                        });
                    }

                    return r;
                }

                ArrayList<NumberTree> _serialise_result(NumberTree result) {
                    ArrayList<ArrayList<NumberTree>> childparts = new ArrayList<ArrayList<NumberTree>>();

                    for (int i = 0; i < result.sources.length; i++) {
                        NumberTree child = result.sources[i];

                        if (child.sources != null && child.sources.length >= 2)
                            childparts.add(_serialise_result(child));
                    }

                    Collections.sort(childparts, new Comparator<ArrayList<NumberTree>>() {
                        int fullsize(ArrayList<NumberTree> n) {
                            int l = 0;
                            for (int i = 0; i < n.size(); i++) {
                                if (n.get(i).sources != null) {
                                    l += n.get(i).sources.length;
                                }
                            }
                            return l + n.size();
                        }

                        public int compare(ArrayList<NumberTree> a, ArrayList<NumberTree> b) {
                            return fullsize(b) - fullsize(a);
                        }
                    });

                    ArrayList<NumberTree> parts = new ArrayList<NumberTree>();
                    for (int i = 0; i < childparts.size(); i++) {
                        parts.addAll(childparts.get(i));
                    }

                    parts.add(result);
                    return parts;
                }

                String _stringify_result(ArrayList<NumberTree> serialised, int target) {
                    String output = "";

                    for (int i = 0; i < serialised.size(); i++) {
                        NumberTree x = serialised.get(i);

                        output += x.sources[0].value;
                        for (int j = 1; j < x.sources.length; j++) {
                            output += " " + x.op + " " + x.sources[j].value;
                        }
                        output += " = " + x.value + "\n";
                    }

                    long result = serialised.get(serialised.size() - 1).value;
                    if (result != target)
                        output += "(off by " + (Math.abs(result - target)) + ")\n";

                    return output;
                }

                char[] OPS = {'+', '-', '*', '/', '_', '?'};
                double[] OP_COST = {1, 1.05, 1.2, 1.3, 1.05, 1.3};

                private void _solve_numbers(NumberTree[] number, int searchedi, boolean[] was_generated, int target, int levels, double valsums) {
                    levels--;

                    if (levels < 0)
                        return;

                    for (int i = 0; i < 5; i++) {
                        NumberTree ni = number[i];
                        if (ni == null)
                            continue;
                        number[i] = null;

                        for (int j = i + 1; j < 6; j++) {
                            NumberTree nj = number[j];
                            if (nj == null)
                                continue;
                            if (i < searchedi && !was_generated[i] && !was_generated[j])
                                continue;

                            for (int k = 0; k < OPS.length; k++) {
                                char o = OPS[k];
                                long rval = compute(o, ni.value, nj.value);
                                if (rval == 424242424242424242l)
                                    continue;

                                long op_cost = Math.abs(rval);
                                while (op_cost % 10 == 0 && op_cost != 0)
                                    op_cost /= 10;
                                if ((ni.value == 10 || nj.value == 10) && o == '*')
                                    op_cost = 1;

                                double newvalsums = valsums + (double) (op_cost) * OP_COST[k];

                                if ((Math.abs(rval - target) < Math.abs(bestresult.value - target))
                                        || (Math.abs(rval - target) == Math.abs(bestresult.value - target) && newvalsums < bestvalsums)) {
                                    bestresult = new NumberTree(o, ni, nj);
                                    bestvalsums = newvalsums;
                                }

                                if (levels > 0 && (bestresult.value != target || newvalsums < bestvalsums)) {
                                    number[j] = new NumberTree(o, ni, nj);
                                    boolean old_was_gen = was_generated[j];
                                    was_generated[j] = true;

                                    _solve_numbers(number, i + 1, was_generated, target, levels, newvalsums);

                                    was_generated[j] = old_was_gen;
                                    number[j] = nj;
                                }
                            }
                        }

                        number[i] = ni;
                    }
                }

                long compute(char op, long a, long b) {
                    boolean is_valid=true;
                    long value = 0;

                    switch(op)

                    {
                        case '+':
                            value = a + b;
                            break;
                        case '-':
                            value = a - b;
                            break;
                        case '*':
                            value = a * b;
                            break;
                        case '_':
                            value = b - a;
                            break;

                        case '/':
                            if (b == 0 || a % b != 0) {
                                is_valid = false;
                                break;
                            }
                            value = a / b;
                            break;

                        case '?':
                            if (a == 0 || b % a != 0) {
                                is_valid = false;
                                break;
                            }
                            value = b / a;
                            break;
                    }

                    if (value < 0)
                        is_valid = false;

                    return is_valid ? value : 424242424242424242l;
                }
            });

            Button reset = (Button)rootView.findViewById(R.id.resetNumbers);
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int[] id = {R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5, R.id.num6};
                    for (int i = 0; i < 6; i++) {
                        EditText edit = (EditText) rootView.findViewById(id[i]);
                        edit.setText("");
                    }
                    numbersResults.setText("");
                    EditText target = (EditText) rootView.findViewById(R.id.target);
                    target.setText("");
                    EditText num1 = (EditText) rootView.findViewById(R.id.num1);
                    num1.requestFocus();
                }
            });
            return rootView;
        }
    }

}
