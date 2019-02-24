package com.example.beefriend;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {
    private boolean aBoolean = true;
    private  ImageView imageView ;
    private Uri uri ;




    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        Create Toolbar
        createToolbar();
        //        Choose ImageView
        chooseImage();

    } //Main Method

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK)
        {
            uri = data.getData();
            aBoolean = false;

            try
            {
                Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri));
                Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap,800,600,false);
                imageView.setImageBitmap(bitmap1);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void chooseImage()
    {
        imageView = getView().findViewById(R.id.imvAvata);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Please Choose App"), 1);


            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.itemUpload) {
            checkValue();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkValue() {
        MyAlert myAlert = new MyAlert(getActivity());

        EditText nameEditText = getView().findViewById(R.id.edtName);
        EditText userEditText = getView().findViewById(R.id.edtUser);
        EditText passwordEditText = getView().findViewById(R.id.edtPassword);

        String name = nameEditText.getText().toString().trim() ;
        String user = userEditText.getText().toString().trim() ;
        String password = passwordEditText.getText().toString().trim() ;


        if (aBoolean)
        {
//           Non Choose Image
            myAlert.normalDialog("Non Choose Image", "Please Choose Avata");
        }
        else if (name.isEmpty() || user.isEmpty() || password.isEmpty())
        {
//            Have Space
            myAlert.normalDialog("No DATA","Please input Data");
        }
        else
        {
//            upload Image To Server
            String pathImageString = null;
            String[] strings  = new String[]{MediaStore.Images.Media.DATA};
            Cursor cursor  = getActivity().getContentResolver().query(uri,strings,null,null,null);
            if (cursor != null)
            {
                cursor.moveToFirst();
                int index =  cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                pathImageString = cursor.getString(index) ;

            }
            else
            {
                pathImageString = uri.getPath();
            }

            Log.d("24FebV1","path ==> "+ pathImageString);
            String nameImage = pathImageString.substring(pathImageString.lastIndexOf("/"));
            Log.d("24FebV1","NameImage"+nameImage);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);


            File file = new File(pathImageString);
            FTPClient ftpClient = new FTPClient();

            try
            {
                ftpClient.connect("ftp.androidthai.in.th",21);
                ftpClient.login("ksu@androidthai.in.th","Abc12345");

                ftpClient.changeDirectory("zumaker");
                ftpClient.upload(file, new uploadListener());
//                update Database
                AddUserThread addUserThread = new AddUserThread(getActivity());
                addUserThread.execute(name,user,password,"http://androidthai.in.th/ksu/zumaker/"+nameImage);
                String result = addUserThread.get() ;

                if (Boolean.parseBoolean(result))
                {
                    getActivity().getSupportFragmentManager().popBackStack();
                }



            }
            catch (Exception e)
            {
                e.printStackTrace();
                try
                {
                    ftpClient.disconnect(true);
                } catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }


        }






    }//checkValue

    public class uploadListener implements FTPDataTransferListener
    {

        @Override
        public void started() {
            Toast.makeText(getActivity(),"Start Upload",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void transferred(int i) {
            Toast.makeText(getActivity(),"Continue Uplaad.....",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void completed() {
            Toast.makeText(getActivity(),"Completed Upload",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void aborted() {
            Toast.makeText(getActivity(),"Stop Upload",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failed() {
            Toast.makeText(getActivity(),"Error!!!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_register, menu);

    }

    private void createToolbar() {
        Toolbar toolbar = getView().findViewById(R.id.toolbarRegister);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Register");
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

}
