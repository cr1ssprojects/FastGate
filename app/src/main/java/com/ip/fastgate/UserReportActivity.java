package com.ip.fastgate;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class UserReportActivity extends AppCompatActivity {
    private Button btnBack;
    private ListView listView;
    private ArrayList<UserReport> report = new ArrayList<>();
    private ArrayList<String> reportInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_user_report);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ListView listView = (ListView) findViewById(R.id.report_lisView);

        PopulateData("SM-21-MAN", "30-05-2022", "07:58", "16:01");
        PopulateData("SM-21-MAN", "26-05-2022", "07:57", "16:02");
        PopulateData("SM-21-MAN", "19-05-2022", "07:55", "16:01");
        PopulateData("SM-21-MAN", "18-05-2022", "07:59", "16:00");
        PopulateData("SM-21-MAN", "17-05-2022", "07:57", "16:05");
        PopulateData("SM-21-MAN", "16-05-2022", "07:52", "16:03");
        PopulateData("SM-21-MAN", "15-05-2022", "07:53", "16:09");

        ArrayAdapter<String> reportAdapter = new ArrayAdapter<String>(this, R.layout.reporttextview, reportInfo);
        listView.setAdapter(reportAdapter);
    }

    public void PopulateData(String marca, String data, String ora_intrare, String ora_iesire) {

        UserReport info = new UserReport(marca, data, ora_intrare, ora_iesire);
        report.add(info);
        reportInfo.add(info.toString(Integer.toString(info.getIndex()), info.getMarca(), info.getData(), info.getOra_intrare(), info.getOra_iesire()));
    }

}
