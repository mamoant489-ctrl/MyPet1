package com.example.mypet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypet.Note;
import com.example.mypet.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    private List<Note> notes;
    private Context context;
    private String userId;
    private String petId;
    private OnNoteChangedListener listener; // Интерфейс для обновления списка в Activity

    public interface OnNoteChangedListener {
        void onNotesChanged();
    }

    public NotesAdapter(Context context, List<Note> notes, String petId, OnNoteChangedListener listener) {
        this.context = context;
        this.notes = new ArrayList<>(notes);
        this.petId = petId;
        this.listener = listener;
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.textView.setText(note.text);
        String reminderStr = note.reminderTime != null && !note.reminderTime.isEmpty()
                ? "Напоминание: " + note.reminderTime
                : "Без напоминания";
        holder.reminderView.setText(reminderStr);

        // Клик по карточке - редактирование
        holder.cardView.setOnClickListener(v -> editNote(position));

        // Удаление
        holder.deleteBtn.setOnClickListener(v -> showDeleteConfirmDialog(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    private void editNote(int position) {
        Note note = notes.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(android.R.layout.simple_list_item_2, null); // Простой layout или ваш custom
        EditText editText = new EditText(context);
        editText.setText(note.text);
        editText.setHint("Редактировать заметку");

        builder.setTitle("Редактировать заметку")
                .setView(editText)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newText = editText.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        updateNote(note.id, newText, note.reminderTime);
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDeleteConfirmDialog(int position) {
        Note note = notes.get(position);
        new AlertDialog.Builder(context)
                .setTitle("Удалить заметку?")
                .setMessage(note.text)
                .setPositiveButton("Да", (dialog, which) -> deleteNote(note.id))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void updateNote(String noteId, String newText, String reminderTime) {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("pets").child(petId).child("remaining").child("notes").child(noteId);
        Note updatedNote = new Note(newText, reminderTime);
        notesRef.setValue(updatedNote).addOnSuccessListener(aVoid -> {
            Toast.makeText(context, "Заметка обновлена", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onNotesChanged();
        });
    }

    private void deleteNote(String noteId) {
        FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).child("pets").child(petId).child("remaining").child("notes")
                .child(noteId).removeValue().addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Заметка удалена", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onNotesChanged();
                });
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes.clear();
        this.notes.addAll(newNotes);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView, reminderView;
        Button deleteBtn;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.noteText);
            reminderView = itemView.findViewById(R.id.reminderText);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
