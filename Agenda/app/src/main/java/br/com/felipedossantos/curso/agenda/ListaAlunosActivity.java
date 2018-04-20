package br.com.felipedossantos.curso.agenda;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.List;

import br.com.felipedossantos.curso.agenda.WebClient.EnviaDadosServidor;
import br.com.felipedossantos.curso.agenda.WebClient.WebClient;
import br.com.felipedossantos.curso.agenda.adapter.AlunosAdapter;
import br.com.felipedossantos.curso.agenda.converter.AlunoConverter;
import br.com.felipedossantos.curso.agenda.dao.AlunoDAO;
import br.com.felipedossantos.curso.agenda.modelo.Aluno;

public class ListaAlunosActivity extends AppCompatActivity {

    public static final int CODE_PERM_CALL = 1;
    public static final int CODE_PERM_SMS = 2;
    private ListView listaAlunos;


    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS } , CODE_PERM_SMS);
        }


        listaAlunos = (ListView) findViewById(R.id.lista_alunos);

        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> lista, View item, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);

                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                intentVaiProFormulario.putExtra("aluno", aluno);
                startActivity(intentVaiProFormulario);
            }
        });

        Button novoAluno = (Button) findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(intentVaiProFormulario);
            }
        });

        registerForContextMenu(listaAlunos);

    }

    @Override
    protected void onResume() {
        super.onResume();

        carregaLista();
    }

    private void carregaLista() {
        AlunoDAO dao = new AlunoDAO(this);
        List<Aluno> alunos = dao.buscaalunos();
        dao.close();

        //ArrayAdapter<Aluno> adapter = new ArrayAdapter<Aluno>(this, android.R.layout.simple_list_item_1, alunos);
        AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        // Capturando informações do menu de contexto acionado
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // Extraido posição do item do menu de contexto acionado e com base nisto capturado o objeto do Aluno
            final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);

        // Acidionado itens ao Menu de Contexto
            MenuItem itemLigar = menu.add("Ligar");
            MenuItem itemSMS = menu.add("SMS");
            MenuItem itemSite = menu.add("Visitar Site");
            MenuItem itemMapa = menu.add("Ver End. no Mapa");
            MenuItem itemDeletar = menu.add("Deletar");

        //Ação do Item de Ligar
            itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE}, CODE_PERM_CALL);
                    } else {
                        Intent intentLigar = new Intent(Intent.ACTION_CALL);
                        intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                        startActivity(intentLigar);
                    }
                    return false;
                }
            });

        // Ação do Item de Envio de SMS
            Intent intentSMS = new Intent(Intent.ACTION_VIEW);
            intentSMS.setData(Uri.parse("sms:" + aluno.getTelefone()));
            itemSMS.setIntent(intentSMS);

        // Ação do Item de acesso ao Site do Aluno
            Intent intentSite = new Intent(Intent.ACTION_VIEW);
            String site = aluno.getSite();
            if(!site.startsWith("http://") & !site.startsWith("https://")) {
                site = "http://" + site;
            }
            intentSite.setData(Uri.parse(site));
            itemSite.setIntent(intentSite);

        // Ação de Item de visualização do endereço no Mapa
            Intent intentMapa = new Intent(Intent.ACTION_VIEW);
            intentMapa.setData(Uri.parse("geo:0,0?z=14&q=" + aluno.getEndereco()));
            itemMapa.setIntent(intentMapa);

        // Ação do Item de Deletar
            itemDeletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AlunoDAO dao = new AlunoDAO(ListaAlunosActivity.this);
                    dao.deleta(aluno);
                    dao.close();

                    carregaLista();

                    Toast.makeText(ListaAlunosActivity.this, "Deletado o Aluno " + aluno.getNome() + "!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.enviar_notas :
                new EnviaDadosServidor(this).execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // Metodo de retorno a cada solicitação de permissão, aqui que deve ser tratado o que fará logo após o usuário definir se permite ou não.
        // requestCode tem o Int passado no metodo ActivityCompat.requestPermissions
        // resquestCode criados no fonte:
        // 1 = CODE_PERM_CALL = Acesso para realizar Chamada Telefonica do Aluno.
        // 2 = CODE_PERM_SMS = Acesso para receber SMS.
    }
}
