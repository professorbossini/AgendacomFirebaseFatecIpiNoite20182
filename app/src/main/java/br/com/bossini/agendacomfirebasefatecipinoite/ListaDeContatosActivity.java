package br.com.bossini.agendacomfirebasefatecipinoite;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ListaDeContatosActivity extends AppCompatActivity {

    private List<Contato> contatos;
    private ListView contatosListView;
    private ArrayAdapter <Contato> contatosAdapter;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference contatosReference;


    private void configuraOnItemLongClick (){
        contatosListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                AlertDialog.Builder alertBuilder =
                        new AlertDialog.Builder(ListaDeContatosActivity.this);
                alertBuilder.setPositiveButton(R.string.deletar_contato, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Contato contato = contatos.get(position);
                        contatosReference.child(contato.getId()).removeValue();
                        Toast.makeText(ListaDeContatosActivity.this,
                                getString(R.string.contato_removido),
                                Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(R.string.atualizar_contato, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LayoutInflater inflater =
                                LayoutInflater.from(ListaDeContatosActivity.this);
                        View arvore = inflater.inflate(R.layout.activity_adiciona_contato, null);
                        AlertDialog.Builder atualizarBuilder =
                                new AlertDialog.Builder(ListaDeContatosActivity.this);
                        atualizarBuilder.setView(arvore);
                        final AlertDialog atualizarDialogo = atualizarBuilder.create();
                        final EditText nomeEditText = arvore.findViewById(R.id.nomeEditText);
                        final EditText foneEditText = arvore.findViewById(R.id.foneEditText);
                        final EditText emailEditText = arvore.findViewById(R.id.emailEditText);
                        final Contato contato = contatos.get(position);
                        nomeEditText.setText(contato.getNome());
                        foneEditText.setText(contato.getFone());
                        emailEditText.setText(contato.getEmail() );
                        FloatingActionButton floatingActionButton =
                                arvore.findViewById(R.id.fab);
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                contato.setNome(nomeEditText.getEditableText().toString());
                                contato.setFone(foneEditText.getEditableText().toString());
                                contato.setEmail(emailEditText.getEditableText().toString());
                                contatosReference.child(contato.getId()).setValue(contato);
                                Toast.makeText(ListaDeContatosActivity.this,
                                        getString(R.string.atualizar_contato),
                                        Toast.LENGTH_SHORT).show();
                                atualizarDialogo.cancel();
                            }
                        });
                        atualizarDialogo.show();
                    }
                }).create().show();

                return false;
            }
        });
    }

    private void configuraFirebase (){
        firebaseDatabase = FirebaseDatabase.getInstance();
        contatosReference = firebaseDatabase.getReference("contatos");
    }

    @Override
    protected void onStart() {
        super.onStart();
        contatosReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contatos.clear();
                for (DataSnapshot filho : dataSnapshot.getChildren()){
                    Contato contato = filho.getValue(Contato.class);
                    contato.setId(filho.getKey());
                    contatos.add(contato);
                }
                Collections.sort(contatos, new Comparator<Contato>() {
                    @Override
                    public int compare(Contato o1, Contato o2) {
                        return o1.getNome().compareTo(o2.getNome());
                    }
                });
                contatosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_de_contatos);
        contatos = new LinkedList<> ();
        contatosListView = findViewById(R.id.contatosListView);
        contatosAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        contatos);
        contatosListView.setAdapter(contatosAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent =
                        new Intent(ListaDeContatosActivity.this,
                                    AdicionaContatoActivity.class);
                startActivity(intent);
            }
        });
        configuraFirebase();
        configuraOnItemLongClick();
    }


}
