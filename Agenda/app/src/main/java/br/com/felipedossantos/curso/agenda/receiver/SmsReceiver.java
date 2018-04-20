package br.com.felipedossantos.curso.agenda.receiver;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;
import android.widget.Toast;

import br.com.felipedossantos.curso.agenda.R;
import br.com.felipedossantos.curso.agenda.dao.AlunoDAO;

public class SmsReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");
        byte[] pdu = (byte[]) pdus[0];

        String format = (String) intent.getSerializableExtra("format");
        SmsMessage message = SmsMessage.createFromPdu(pdu, format);
        String telefone = message.getDisplayOriginatingAddress();

        AlunoDAO dao = new AlunoDAO(context);
        // TODO Find how it works to check the region code (DDD) and international code (DDI)
        if (dao.ehAluno(telefone)) {
            Toast.makeText(context, "Chegou um SMS de Aluno!", Toast.LENGTH_SHORT).show();
            MediaPlayer mp = MediaPlayer.create(context, R.raw.msg);
            mp.start();
        }
        dao.close();
    }
}