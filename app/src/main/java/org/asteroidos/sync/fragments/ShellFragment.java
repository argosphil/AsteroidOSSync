/*
 * AsteroidOSSync
 * Copyright (c) 2023 AsteroidOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.asteroidos.sync.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.asteroidos.sync.MainActivity;
import org.asteroidos.sync.R;

import java.nio.charset.StandardCharsets;

public class ShellFragment extends Fragment {
    private final MainActivity mMainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shell, parent, false);
    }

    private boolean submitCommand(EditText editText) {
        Intent intent = new Intent("org.asteroidos.sync.SHELL_TERM_LISTENER");
        String cmd = editText.getText().toString() + "\n";
        if (cmd.equals("\n")) {
            return false;
        }
        handleShellOutput(cmd.getBytes(StandardCharsets.UTF_8));
        intent.putExtra("data", cmd.getBytes(StandardCharsets.UTF_8));
        requireActivity().sendBroadcast(intent);
        editText.setText("");
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        EditText editText = view.findViewById(R.id.shellCmd);
        view.findViewById(R.id.card_view_shell_prompt).setOnClickListener(v -> {
            submitCommand(editText);
        });
        editText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                return submitCommand(editText);
            }
            return false;
        });
    }

    public void handleShellOutput(byte[] byteArray) {
        TextView textView = getView().findViewById(R.id.shell_log);
        textView.setText(textView.getText() + new String(byteArray));
        EditText editText = getView().findViewById(R.id.shellCmd);
        boolean hadFocus = editText.hasFocus();
        ScrollView scrollView = getView().findViewById(R.id.shell_scroll);
        scrollView.fullScroll(View.FOCUS_DOWN);
        if (hadFocus) {
            editText.requestFocus();
        }
    }

    public ShellFragment(MainActivity mainActivity)
    {
        mMainActivity = mainActivity;
    }
}
