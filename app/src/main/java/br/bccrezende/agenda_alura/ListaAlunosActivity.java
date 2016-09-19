package br.bccrezende.agenda_alura;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import br.bccrezende.agenda_alura.adapter.AlunosAdapter;
import br.bccrezende.agenda_alura.dao.AlunoDAO;
import br.bccrezende.agenda_alura.modelo.Aluno;

public class ListaAlunosActivity extends AppCompatActivity {

    private ListView listaAlunos;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        listaAlunos = (ListView) findViewById(R.id.lista_alunos);

        //carregaLista();

        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> lista, View item, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);
                Toast.makeText(ListaAlunosActivity.this, "Aluno " + aluno.getNome() + " clicado", Toast.LENGTH_SHORT).show();

                Intent vaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                vaiProFormulario.putExtra("aluno", aluno);
                startActivity(vaiProFormulario);
            }
        });

        Button novoAluno = (Button) findViewById(R.id.novo_aluno);

        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentvaiproformulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(intentvaiproformulario);
            }
        });

        registerForContextMenu(listaAlunos);

    }

    private void carregaLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaAlunos();
        dao.close();

        AlunosAdapter adapter = new AlunosAdapter(this, alunos);

        listaAlunos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregaLista();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);

        /* CRIANDO A OPÇÃO PARA LIGAR PARA O CONTATO VIA MENU DE CONTEXTO NA AGENDA*/
        MenuItem itemLigar = menu.add("Ligar");
        itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 if (ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE)
                         != PackageManager.PERMISSION_GRANTED) {
                     ActivityCompat.requestPermissions(ListaAlunosActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 123);
                 } else {
                     Intent ligar = new Intent(Intent.ACTION_CALL);
                     ligar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                     startActivity(ligar);
                 }
                 return false;
             }
         });

        /* CRIANDO A OPÇÃO PARA ENVIAR SMS VIA MENU DE CONTEXTO NA AGENDA*/
        MenuItem itemSMS = menu.add("Enviar SMS");
        Intent intentSMS = new Intent(Intent.ACTION_VIEW);
        intentSMS.setData(Uri.parse("sms:" + aluno.getTelefone()));
        itemSMS.setIntent(intentSMS);

        /* CRIANDO A OPÇÃO PARA ACESSAR O ENDEREÇO VIA MENU DE CONTEXTO NA AGENDA*/
        MenuItem itemMapa = menu.add("Visualizar no Mapa");
        Intent intentMapa = new Intent(Intent.ACTION_VIEW);
        intentMapa.setData(Uri.parse("geo:0,0?q=" + aluno.getEndereco()));
        itemMapa.setIntent(intentMapa);

        /* CRIANDO A OPÇÃO PARA ACESSAR O SITE VIA MENU DE CONTEXTO NA AGENDA*/

        MenuItem itemSite = menu.add("Visitar site");
        Intent intentSite = new Intent(Intent.ACTION_VIEW);

        String site = aluno.getSite();
        if (!site.startsWith("http://")){
            site = "http://" + site;
        }
        intentSite.setData(Uri.parse(site));
        itemSite.setIntent(intentSite);


        /* CRIANDO A OPÇÃO PARA DELETAR O ALUNO VIA MENU DE CONTEXTO NA AGENDA*/

        MenuItem deletar = menu.add("Excluir");
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


                AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                dao.deleta(aluno);
                dao.close();
                carregaLista();

                Toast.makeText(ListaAlunosActivity.this, "Deletar o aluno" + aluno.getNome(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }
}


