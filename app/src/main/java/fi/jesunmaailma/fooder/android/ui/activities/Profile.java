package fi.jesunmaailma.fooder.android.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.services.FooderDataService;

public class Profile extends AppCompatActivity {
    ImageView ivProfilePic;
    TextView tvUsername, tvEmail;

    MaterialButton btnSignIn, btnSignOut, btnEditEmail, btnEditName, btnEditPassword, btnDeleteAccount;

    FirebaseAuth auth;
    FirebaseFirestore database;
    FirebaseUser user;
    FirebaseAnalytics analytics;
    DocumentReference documentReference;
    GoogleSignInClient client;
    ActionBar actionBar;
    Toolbar toolbar;
    boolean notificationsEnabled = false;
    SwitchMaterial swNotifications;
    LayoutInflater inflater;

    FooderDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        swNotifications = findViewById(R.id.sw_notifications);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        notificationsEnabled = sharedPref.getBoolean("notification",false);

        actionBar = getSupportActionBar();

        inflater = this.getLayoutInflater();

        ivProfilePic = findViewById(R.id.iv_profile);
        tvUsername = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        btnSignIn = findViewById(R.id.btn_login_link);
        btnSignOut = findViewById(R.id.btn_sign_out);
        btnEditEmail = findViewById(R.id.btn_edit_email);
        btnEditName = findViewById(R.id.btn_edit_name);
        btnEditPassword = findViewById(R.id.btn_edit_password);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        client = GoogleSignIn.getClient(Profile.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);
        database = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        service = new FooderDataService(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "name");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (user == null) {
            swNotifications.setVisibility(View.GONE);
            tvUsername.setText("Hei tyyppi, et ole kirjautunut sisään.");
            tvEmail.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.VISIBLE);
            btnEditEmail.setVisibility(View.GONE);
            btnEditName.setVisibility(View.GONE);
            btnEditPassword.setVisibility(View.GONE);
            btnDeleteAccount.setVisibility(View.GONE);
        } else {
            swNotifications.setChecked(notificationsEnabled);

            swNotifications.setVisibility(View.VISIBLE);
            swNotifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (swNotifications.isChecked()) {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                service.addTokenForNotifications(
                                        String.format(
                                                "%sAddTokenForNotifications?Token=%s",
                                                getResources().getString(R.string.digiruokalista_api_base_url),
                                                task.getResult()
                                        ), new FooderDataService.OnTokenAddedForNotificationsResponse() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Ilmoitukset sallittu.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                                SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sp.edit();
                                                editor.putBoolean("notification",true);
                                                editor.apply();
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Virhe!",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                                swNotifications.setChecked(false);
                                            }
                                        });

                            }
                        });
                    } else {
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                service.deleteTokenFromNotifications(String.format(
                                        "%sRemoveTokenForNotifications?Token=%s",
                                        getResources().getString(R.string.digiruokalista_api_base_url),
                                        task.getResult()
                                ), new FooderDataService.OnTokenDeletedFromNotificationsResponse() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "Ilmoitukset estetty.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putBoolean("notification",false);
                                        editor.apply();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Toast.makeText(
                                                getApplicationContext(),
                                                "Virhe!",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        swNotifications.setChecked(true);
                                    }
                                });
                            }
                        });
                    }
                }
            });
            tvEmail.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.GONE);
            btnEditEmail.setVisibility(View.VISIBLE);
            btnEditName.setVisibility(View.VISIBLE);
            btnEditPassword.setVisibility(View.VISIBLE);
            btnDeleteAccount.setVisibility(View.VISIBLE);

            documentReference = database.collection("Users").document(user.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        ivProfilePic.setImageResource(R.drawable.ic_account);
                        tvUsername.setText(snapshot.getString("firstName"));
                        tvEmail.setText(snapshot.getString("email"));
                    } else {
                        tvUsername.setText(getFirstName(Objects.requireNonNull(user.getDisplayName())));
                        tvEmail.setText(user.getEmail());
                    }
                }
            });
        }

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext()
                        , LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOutDialog(Profile.this);
            }
        });

        btnEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditEmailDialog(Profile.this);
            }
        });

        btnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditNameDialog(Profile.this);
            }
        });

        btnEditPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPasswordDialog(Profile.this);
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAccountDialog(Profile.this);
            }
        });
    }

    public String getFirstName(String fullName) {
        int index = fullName.lastIndexOf(" ");
        if (index > -1) {
            return fullName.substring(0, index);
        }
        return fullName;
    }

    public String getLastName(String fullName) {
        int index = fullName.lastIndexOf(" ");
        if (index > -1) {
            return fullName.substring(index + 1, fullName.length());
        }
        return "";
    }

    private void SignOutDialog(final Activity activity) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(document.getString("firstName"));
                    builder.setMessage("Haluatko varmasti kirjautua ulos Fooder-palvelusta?");
                    builder.setPositiveButton("Kirjaudu ulos", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth.signOut();
                            client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(getApplicationContext(), OnboardingActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(getFirstName(Objects.requireNonNull(user.getDisplayName())));
                    builder.setMessage("Haluatko varmasti kirjautua ulos Fooder-palvelusta?");
                    builder.setPositiveButton("Kirjaudu ulos", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth.signOut();
                            client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(getApplicationContext(), OnboardingActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void EditEmailDialog(Activity activity) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    View view = inflater.inflate(R.layout.update_email_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Päivitä sähköposti")
                            .setMessage("Päivitä sähköpostisi täyttämällä alla oleva kenttä.")
                            .setPositiveButton("Päivitä", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EditText email = view.findViewById(R.id.emailEdit);

                                    if (email.getText().toString().isEmpty()) {
                                        email.setError("Sähköposti vaaditaan.");
                                        return;
                                    }

                                    user.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            Map<String, Object> userData = new HashMap<>();

                                            userData.put("email", email.getText().toString());

                                            documentReference.update(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(
                                                                getApplicationContext(),
                                                                "Sähköposti päivitetty.",
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                        startActivity(
                                                                new Intent(getApplicationContext(), MainActivity.class)
                                                                        .addFlags(
                                                                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                                        ));
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Virhe tapahtui jostain randomista syystä: " +
                                                            e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    });
                                }
                            }).setNegativeButton("Peruuta", null)
                            .setView(view)
                            .create().show();
                } else {
                    View view = inflater.inflate(R.layout.update_email_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Päivitä sähköposti")
                            .setMessage("Päivitä sähköpostisi täyttämällä alla oleva kenttä.")
                            .setPositiveButton("Päivitä", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    EditText email = view.findViewById(R.id.emailEdit);

                                    if (email.getText().toString().isEmpty()) {
                                        email.setError("Sähköposti vaaditaan.");
                                        return;
                                    }

                                    user.updateEmail(email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Sähköposti päivitetty.",
                                                    Toast.LENGTH_LONG
                                            ).show();
                                            startActivity(
                                                    new Intent(getApplicationContext(), MainActivity.class)
                                                            .addFlags(
                                                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                            ));
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Virhe tapahtui jostain randomista syystä: " +
                                                            e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    });

                                }
                            }).setNegativeButton("Peruuta", null)
                            .setView(view)
                            .create().show();
                }
            }
        });
    }

    private void EditNameDialog(Activity activity) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    View view = inflater.inflate(R.layout.update_name_dialog, null);

                    EditText firstName = view.findViewById(R.id.firstNameEdit);
                    EditText lastName = view.findViewById(R.id.lastNameEdit);

                    firstName.setText(document.getString("firstName"));
                    lastName.setText(document.getString("lastName"));

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Päivitä nimi")
                            .setMessage("Päivitä nimesi täyttämällä alla olevat kentät.")
                            .setPositiveButton("Päivitä", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (firstName.getText().toString().isEmpty()) {
                                        firstName.setError("Etunimi vaaditaan.");
                                        return;
                                    }

                                    if (lastName.getText().toString().isEmpty()) {
                                        lastName.setError("Sukunimi vaaditaan.");
                                        return;
                                    }

                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(
                                                    String.format(
                                                            "%s %s",
                                                            firstName.getText().toString(),
                                                            lastName.getText().toString()
                                                    )
                                            )
                                            .build();

                                    user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            Map<String, Object> userData = new HashMap<>();

                                            userData.put("firstName", firstName.getText().toString());
                                            userData.put("lastName", lastName.getText().toString());

                                            documentReference.update(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(
                                                                getApplicationContext(),
                                                                "Nimi päivitetty.",
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                        startActivity(
                                                                new Intent(getApplicationContext(), MainActivity.class)
                                                                        .addFlags(
                                                                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                                        ));
                                                        finish();
                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Virhe tapahtui jostain randomista syystä: " +
                                                            e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    });
                                }
                            }).setNegativeButton("Peruuta", null)
                            .setView(view)
                            .create().show();
                } else {
                    View view = inflater.inflate(R.layout.update_name_dialog, null);

                    EditText firstName = view.findViewById(R.id.firstNameEdit);
                    EditText lastName = view.findViewById(R.id.lastNameEdit);

                    firstName.setText(getFirstName(Objects.requireNonNull(user.getDisplayName())));
                    lastName.setText(getLastName(Objects.requireNonNull(user.getDisplayName())));

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Päivitä nimi")
                            .setMessage("Päivitä nimesi täyttämällä alla olevat kentät.")
                            .setPositiveButton("Päivitä", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (firstName.getText().toString().isEmpty()) {
                                        firstName.setError("Etunimi vaaditaan.");
                                        return;
                                    }

                                    if (lastName.getText().toString().isEmpty()) {
                                        lastName.setError("Sukunimi vaaditaan.");
                                        return;
                                    }

                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(
                                                    String.format(
                                                            "%s %s",
                                                            firstName.getText().toString(),
                                                            lastName.getText().toString()
                                                    )
                                            ).build();

                                    user.updateProfile(userProfileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Nimi päivitetty.",
                                                    Toast.LENGTH_LONG
                                            ).show();
                                            startActivity(
                                                    new Intent(getApplicationContext(), MainActivity.class)
                                                            .addFlags(
                                                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                            ));
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Virhe tapahtui jostain randomista syystä: " +
                                                            e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    });
                                }
                            }).setNegativeButton("Peruuta", null)
                            .setView(view)
                            .create().show();
                }
            }
        });
    }

    private void EditPasswordDialog(Activity activity) {
        View view = inflater.inflate(R.layout.update_password_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Päivitä salasana")
                .setMessage("Päivitä salasanasi täyttämällä alla olevat kentät.")
                .setPositiveButton("Päivitä", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EditText password = view.findViewById(R.id.passwordEdit);
                        EditText passwordConf = view.findViewById(R.id.passwordConfEdit);

                        if (password.getText().toString().isEmpty()) {
                            password.setError("Salasana vaaditaan.");
                            return;
                        }

                        if (password.getText().toString().length() < 8) {
                            password.setError("Salasanan täytyy olla 8 merkin pituinen.");
                            return;
                        }

                        if (!password.getText().toString().equals(passwordConf.getText().toString())) {
                            password.setError("Salasanat eivät täsmää.");
                            return;
                        }

                        user.updatePassword(password.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Salasana päivitetty.",
                                        Toast.LENGTH_LONG
                                ).show();
                                startActivity(
                                        new Intent(getApplicationContext(), MainActivity.class)
                                                .addFlags(
                                                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                ));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Virhe tapahtui jostain randomista syystä: " +
                                                e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });

                    }
                }).setNegativeButton("Peruuta", null)
                .setView(view)
                .create().show();
    }

    private void DeleteAccountDialog(Activity activity) {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    builder.setTitle(
                                    String.format(
                                            "%s\n(%s)",
                                            document.getString("firstName"),
                                            document.getString("email")
                                    )
                            )
                            .setMessage(
                                    String.format(
                                            "%s\n\n%s\n\n%s",
                                            "Haluatko varmasti poistaa tämän tilin?",
                                            "Poistaminen tyhjentää kirjautumistietosi, jolloin et enää pääse kirjautumaan tilillesi.",
                                            "Voit aina luoda uuden tilin, jos kadut tilisi poistamista."
                                    )
                            )
                            .setPositiveButton("Kyllä, poista", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(
                                                                getApplicationContext(),
                                                                "Tilisi on poistettu onnistuneesti.\nHyvää päivänjatkoa.",
                                                                Toast.LENGTH_LONG
                                                        ).show();
                                                        auth.signOut();
                                                        updateUI();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Älä poista", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    builder.setTitle(
                                    String.format(
                                            "%s\n(%s)",
                                            getFirstName(Objects.requireNonNull(user.getDisplayName())),
                                            user.getEmail()
                                    )
                            )
                            .setMessage(
                                    String.format(
                                            "%s\n\n%s\n\n%s",
                                            "Haluatko varmasti poistaa tämän tilin?",
                                            "Poistaminen tyhjentää kirjautumistietosi, jolloin et enää pääse kirjautumaan tilillesi.",
                                            "Voit aina luoda uuden tilin, jos kadut tilisi poistamista."
                                    )
                            )
                            .setPositiveButton("Kyllä, poista", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Tilisi on poistettu onnistuneesti.\nHyvää päivänjatkoa.",
                                                        Toast.LENGTH_LONG
                                                ).show();
                                                auth.signOut();
                                                client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                        updateUI();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }).setNegativeButton("Älä poista", null)
                            .create().show();
                }
            }
        });
    }

    private void updateUI() {
        startActivity(
                new Intent(
                        getApplicationContext(), OnboardingActivity.class
                ).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
        );
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}