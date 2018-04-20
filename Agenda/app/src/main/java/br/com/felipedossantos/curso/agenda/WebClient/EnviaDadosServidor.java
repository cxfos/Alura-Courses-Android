package br.com.felipedossantos.curso.agenda.WebClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.List;

import br.com.felipedossantos.curso.agenda.converter.AlunoConverter;
import br.com.felipedossantos.curso.agenda.dao.AlunoDAO;
import br.com.felipedossantos.curso.agenda.modelo.Aluno;

public class EnviaDadosServidor extends AsyncTask<Void, Void, String> {

    private Context context;
    private ProgressDialog alertDialog;

    public EnviaDadosServidor( Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        alertDialog = ProgressDialog.show(context,"Aguarde" , "Enviando para o servidor ...", true, true);
        alertDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {

        WebClient webClient = new WebClient();
        AlunoConverter converter = new AlunoConverter();
        AlunoDAO dao = new AlunoDAO(context);
        List<Aluno> alunos = dao.buscaalunos();
        dao.close();
        String json = converter.toJson(alunos);
        return webClient.post(json);
    }

    @Override
    protected void onPostExecute(String resposta) {
        alertDialog.dismiss();
        Toast.makeText(context, resposta, Toast.LENGTH_LONG).show();
    }

}
