package com.example.mypet.interfaces;

import com.example.mypet.models.Command;

public interface CommandClickListener {
    void onCommandEdit(Command command);
    void onCommandDelete(Command command);
}
