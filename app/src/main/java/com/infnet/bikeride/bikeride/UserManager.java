package com.infnet.bikeride.bikeride;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Imperiali on 21/07/18.
 */

public class UserManager {
//    Profile profile;
    TextView mDrawerUserName;
    Activity activity;

    private static final String PROFILES_CHILD = "Profiles";

    private FirebaseAccess mFirebase = new FirebaseAccess();

    private FirebaseUser userFirebase;
    private FirebaseAuth autentication;

    public UserManager() {
    }

    public Users user = new Users();
    public Profile profile = new Profile();


    //Interface de acesso ao getPerfil
    public interface OnUserComplete {
        void onUserComplete(Users data);
        void onErrorUserComplete(Users data);
    }


    // Função que Chama o firebaseAccess e faz o fetch direto na child
    public void getPerfil(final OnUserComplete callback, String id) {


//        userFirebase = autentication.getCurrentUser();

        mFirebase.getObjectOrProperty(Users.class,

                new FirebaseAccess.OnComplete<Users>() {
                    //Faz o Retorno do usuário logado
                    @Override
                    public void onSuccess(Users data) {
                        Users currentProfile = data;
                        callback.onUserComplete(data);
//                        return currentProfile;
                    }

                    @Override
                    public void onFailure(Users data) {
                        Log.e("ERRO", "Deu ruim");
                        callback.onErrorUserComplete(data);
                    }
                    //Caminho do nó do Firebase
                },"Profiles",id.toString());
    }

    // Cria o usuário sempre que logar ou atualiza sempre que loga pois está sempre em mudança
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
