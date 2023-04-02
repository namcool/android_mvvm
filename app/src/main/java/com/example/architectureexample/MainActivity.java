package com.example.architectureexample;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

enum ActionDeclaration {
    ADD_ACTION,
    EDIT_ACTION
}

public class MainActivity extends AppCompatActivity {
    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;
    private NoteViewModel noteViewModel;
    private ActionDeclaration action;

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        switch (action) {
                            case ADD_ACTION:
                                // There are no request codes
                                if (data != null) {
                                    String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                                    String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
                                    int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

                                    Note note = new Note(title, description, priority);
                                    noteViewModel.insert(note);

                                    Toast.makeText(getApplicationContext(), "Note saved", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case EDIT_ACTION:
                                int id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);
                                if (id == -1) {
                                    Toast.makeText(getApplicationContext(), "Note can't be updated", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
                                String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
                                int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

                                Note note = new Note(title, description, priority);
                                note.setId(id);
                                noteViewModel.update(note);
                                Toast.makeText(getApplicationContext(), "Note updated", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Note not saved", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton buttonAddNote = findViewById(R.id.btn_add_note);
        buttonAddNote.setOnClickListener(view -> {
            action = ActionDeclaration.ADD_ACTION;
            Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
            someActivityResultLauncher.launch(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        NoteAdapter noteAdapter = new NoteAdapter();
        recyclerView.setAdapter(noteAdapter);

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                // update RecycleView later
                noteAdapter.setNotes(notes);
                Toast.makeText(MainActivity.this, "onChanged", Toast.LENGTH_SHORT).show();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(noteAdapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Note at " + viewHolder.getAdapterPosition() + " was deleted", Toast.LENGTH_SHORT ).show();;
            }
        }).attachToRecyclerView(recyclerView);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                action = ActionDeclaration.EDIT_ACTION;
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);

                intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
                intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
                intent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.getDescription());
                intent.putExtra(AddEditNoteActivity.EXTRA_PRIORITY, note.getPriority());

                someActivityResultLauncher.launch(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_notes:
                deleteAllNotes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllNotes() {
        noteViewModel.deleteAllNotes();
    }
}