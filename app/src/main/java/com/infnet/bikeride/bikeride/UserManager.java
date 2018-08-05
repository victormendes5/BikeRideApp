package com.infnet.bikeride.bikeride;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by Imperiali on 21/07/18.
 */

public class UserManager {
//    Profile profile;
    TextView mDrawerUserName;
    Activity activity;

    private static final String PROFILES_CHILD = "Profiles";

    private FirebaseAccess mFirebase = new FirebaseAccess();

    public UserManager() {
    }

    public Users user = new Users();
    public Profile profile = new Profile();


    private void  UsuarioLogado(){

//        getPerfil();

    }

    public void getPerfil() {

        mFirebase.getObjectOrProperty(Users.class,

                new FirebaseAccess.OnComplete<Users>() {
                    @Override
                    public Users onSuccess(Users data) {
                        Users currentProfile = data;
                        return currentProfile;
                    }

                    @Override
                    public void onFailure(Users data) {
                        Log.e("ERRO", "Deu ruim");
                        return ;
                    }
                },"Profile");
    }

    public void adicionarOuAtualizarPerfil(final Users profile){

        mFirebase.addOrUpdate(profile,

                new FirebaseAccess.OnCompleteVoid() {
                    @Override
                    public void onSuccess() {
                        Users currentProfile = profile;
                    }

                    @Override
                    public void onFailure() {
                        Log.e("ERRO", "Deu ruim");
                    }

                }, PROFILES_CHILD, profile.getId());
    }

}
