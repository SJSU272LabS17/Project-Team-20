package com.onthespot.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.firebase.dao.Complaint;
import com.firebase.dao.Person;
import com.google.gson.Gson;
import com.onthespot.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DemoConfiguration {

    public Context ctx;
    private static List<DemoFeature> demoFeatures;


    private SharedPreferences sharedpreferences;
    static {

    }

    public DemoConfiguration()
    {}

    public DemoConfiguration(Context con, String userName)
    {
        demoFeatures = new ArrayList<DemoFeature>();

        this.ctx=con;
        sharedpreferences= ctx.getSharedPreferences("pref", Context.MODE_PRIVATE);

        Gson gson = new Gson();


        String json1 = sharedpreferences.getString(userName, "");
        Person obj = gson.fromJson(json1, Person.class);

        System.out.println("..key_name1."+obj.getEmail());

        List<Complaint> complaintList = new ArrayList<Complaint>();

        complaintList = obj.getComplaints();

        if(complaintList!=null && complaintList.size()>0) {

            for (int i=0 ; i<complaintList.size(); i++) {

            Complaint complaint = complaintList.get(i);
            String str = "Complaint"+" "+(i+1);
            String location =  complaint.getComplaintLocation().getStreet()+", "+complaint.getComplaintLocation().getCity() +", "+
                    complaint.getComplaintLocation().getState()+", "+complaint.getComplaintLocation().getCountry()+" /n"+complaint.getComplaintLocation().getZip();


                addDemoFeature("user_identity", R.mipmap.user_identity, str,
                        complaint.getDescription(), "\n\n"+"Authority : "+complaint.getAuthorityName()+"\n\n"+"Complaint Date : "+complaint.getComplaintDate().toString()+"\n\n"
                        +"Image URL : "+complaint.getComplaintImage()+"\n\n"+"Location : "+location,
                        "", "",
                        new DemoItem(R.string.main_fragment_title_user_identity, R.mipmap.user_identity,
                               userName, IdentityDemoFragment.class));

            }
        }

    }

    public static List<DemoFeature> getDemoFeatureList() {
        return demoFeatures;
    }

    public static DemoFeature getDemoFeatureByName(final String name) {
        for (DemoFeature demoFeature : demoFeatures) {
            if (demoFeature.name.equals(name)) {
                return demoFeature;
            }
        }
        return null;
    }

    private static void addDemoFeature(final String name, final int iconResId, final String titleResId,
                                       final String subtitleResId, final String overviewResId,
                                       final String descriptionResId, final String poweredByResId,
                                       final DemoItem... demoItems) {
        DemoFeature demoFeature = new DemoFeature(name, iconResId, titleResId, subtitleResId,
                overviewResId, descriptionResId, poweredByResId, demoItems);
        demoFeatures.add(demoFeature);
    }

    public static class DemoFeature {
        public String name;
        public int iconResId;
        public String titleResId;
        public String subtitleResId;
        public String overviewResId;
        public String descriptionResId;
        public String poweredByResId;
        public List<DemoItem> demos;

        public DemoFeature() {

        }

        public DemoFeature(final String name, final int iconResId, final String titleResId,
                           final String subtitleResId, final String overviewResId,
                           final String descriptionResId, final String poweredByResId,
                           final DemoItem... demoItems) {
            this.name = name;
            this.iconResId = iconResId;
            this.titleResId = titleResId;
            this.subtitleResId = subtitleResId;
            this.overviewResId = overviewResId;
            this.descriptionResId = descriptionResId;
            this.poweredByResId = poweredByResId;
            this.demos = Arrays.asList(demoItems);
        }
    }

    public static class DemoItem {
        public int titleResId;
        public int iconResId;
        public String buttonTextResId;
        public String fragmentClassName;

        public String title;
        public String buttonText;
        public Serializable tag ;

        public DemoItem(final int titleResId, final int iconResId, final String buttonTextResId,
                        final Class<? extends Fragment> fragmentClass) {
            this.titleResId = titleResId;
            this.iconResId = iconResId;
            this.buttonTextResId = buttonTextResId;
            this.fragmentClassName = fragmentClass.getName();
        }

        public DemoItem(final String title, final String buttonText, final Serializable tag, final Class<? extends  Fragment> fragmentClass){
            this.title = title;
            this.buttonText = buttonText;
            this.tag = tag;
            this.fragmentClassName = fragmentClass.getName();
        }
    }
}