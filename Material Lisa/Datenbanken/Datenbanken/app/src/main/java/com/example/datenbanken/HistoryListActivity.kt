package com.example.datenbanken

import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.appcompat.app.AppCompatActivity
import android.widget.CursorAdapter
import android.widget.ListView

class HistoryListActivity : AppCompatActivity() {


    private lateinit var ca: CursorAdapter
    private var dbHandler: OpenHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_list)
        val lv: ListView = findViewById<View>(R.id.list) as ListView

        ca = MoodAdapter(this)
        lv.setAdapter(ca)
        registerForContextMenu(lv)

        dbHandler = OpenHandler(this)
        updateList()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHandler!!.close()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.getMenuInfo() as AdapterContextMenuInfo
        return when (item.getItemId()) {
            R.id.menu_good -> {
                dbHandler!!.update(
                    info.id,
                    OpenHandler.MOOD_FINE
                )
                updateList()
                true
            }
            R.id.menu_ok -> {
                dbHandler!!.update(
                    info.id,
                    OpenHandler.MOOD_OK
                )
                updateList()
                true
            }
            R.id.menu_bad -> {
                dbHandler!!.update(
                    info.id,
                    OpenHandler.MOOD_BAD
                )
                updateList()
                true
            }
            R.id.menu_delete -> {
                dbHandler!!.delete(info.id)
                updateList()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun updateList() {
        // Cursor tauschen - der alte wird geschlossen
        ca.changeCursor(dbHandler!!.query())
    }
}