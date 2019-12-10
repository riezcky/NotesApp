package com.example.mynotesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.example.mynotesapp.adapter.NoteAdapter;
import com.example.mynotesapp.db.NoteHelper;
import com.example.mynotesapp.entity.Note;
import com.example.mynotesapp.helper.MappingHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.example.mynotesapp.NoteAddUpdateActivity.REQUEST_UPDATE;

public class MainActivity extends AppCompatActivity implements LoadNotesCallback{
    private ProgressBar progressBar;
    private RecyclerView rvNotes;
    private NoteAdapter adapter;

    private FloatingActionButton fabAdd;
    private NoteHelper noteHelper;
    private static final String EXTRA_STATE = "EXTRA_STATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Notes");
        }

        progressBar = findViewById(R.id.progressbar);
        rvNotes = findViewById(R.id.rv_notes);

        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setHasFixedSize(true);

        adapter = new NoteAdapter(this);
        rvNotes.setAdapter(adapter);

        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteAddUpdateActivity.class);
                startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD);
            }
        });

        noteHelper = NoteHelper.getInstance(getApplicationContext());
        noteHelper.open();

        if(savedInstanceState == null){
            //proses ambil data
            new LoadNotesAsync(noteHelper, this).execute();
        }else{
            ArrayList<Note> list = savedInstanceState.getParcelableArrayList(EXTRA_STATE);
            if(list != null){
                adapter.setListNotes(list);
            }
        }
    }

    @Override
    public void preExecute(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void postExecute(ArrayList<Note> notes) {
        progressBar.setVisibility(View.INVISIBLE);
        if(notes.size() > 0){
            adapter.setListNotes(notes);
        }else{
            adapter.setListNotes(new ArrayList<Note>());
            showSnackBarMessage("Tidak ada data saat ini");
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        noteHelper.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            //akan dipanggil jika request Codenya ADD
            if (requestCode == NoteAddUpdateActivity.REQUEST_ADD) {
                if (resultCode == NoteAddUpdateActivity.RESULT_ADD) {
                    Note note = data.getParcelableExtra(NoteAddUpdateActivity.EXTRA_NOTE);

                    adapter.addItem(note);
                    rvNotes.smoothScrollToPosition(adapter.getItemCount() - 1);

                    showSnackBarMessage("Satu item berhasil ditambahkan");
                }
            }
            // Update dan Delete memiliki request code sama akan tetapi result codenya berbeda
            else if (requestCode == NoteAddUpdateActivity.REQUEST_UPDATE) {
                if (resultCode == NoteAddUpdateActivity.RESULT_UPDATE) {
                    Note note = data.getParcelableExtra(NoteAddUpdateActivity.EXTRA_NOTE);
                    int position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0);

                    adapter.updateItem(position, note);
                    rvNotes.smoothScrollToPosition(position);

                    showSnackBarMessage("Satu item berhasil diubah");
                } else if (resultCode == NoteAddUpdateActivity.RESULT_DELETE) {
                    int position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0);

                    adapter.removeItem(position);

                    showSnackBarMessage("Satu item berhasil dihapus");
                }
            }
        }
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(rvNotes, message, Snackbar.LENGTH_SHORT).show();
    }


    private static class LoadNotesAsync extends AsyncTask<Void, Void, ArrayList<Note>> {

        private final WeakReference<NoteHelper> weakNoteHelper;
        private final WeakReference<LoadNotesCallback> weakCallback;

        private LoadNotesAsync(NoteHelper noteHelper, LoadNotesCallback callback) {
            weakNoteHelper = new WeakReference<>(noteHelper);
            weakCallback = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            weakCallback.get().preExecute();
        }

        @Override
        protected ArrayList<Note> doInBackground(Void... voids) {
            Cursor dataCursor = weakNoteHelper.get().queryAll();
            return MappingHelper.mapCursorToArrayList(dataCursor);
        }

        @Override
        protected void onPostExecute(ArrayList<Note> notes){
            super.onPostExecute(notes);

            weakCallback.get().postExecute(notes);
        }
    }
}

interface LoadNotesCallback{
    void preExecute();
    void postExecute(ArrayList<Note> notes);
}
