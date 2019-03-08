package com.kingtous.remotefingerunlock.ToolFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kingtous.remotefingerunlock.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class about extends Fragment {

    Button btn_url;

    String address="https://github.com/Kingtous/RemoteUnlockMyComputer";

    public about()
    {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=LayoutInflater.from(getContext()).inflate(R.layout.app_about,container,false);
        btn_url=view.findViewById(R.id.btn_welcome_url);

        btn_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(address);
                intent.setData(content_url);
                startActivity(intent);
            }
        });
        return view;

    }
}
