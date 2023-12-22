package bangkit.project.fed.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bangkit.project.fed.R
import bangkit.project.fed.data.EggData
import bangkit.project.fed.data.api.Egg
import com.bumptech.glide.Glide

class LibraryRvAdapter(private val context: Context) :
    RecyclerView.Adapter<LibraryRvAdapter.ViewHolder>()

{

    private val listEgg = mutableListOf<EggData>()
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image : ImageView = itemView.findViewById(R.id.image)
        val label : TextView = itemView.findViewById(R.id.label)
        val date : TextView = itemView.findViewById(R.id.date)
        val menu : ImageView = itemView.findViewById(R.id.menu)
    }

    fun submitList(newList: List<EggData>) {
        listEgg.clear()
        listEgg.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryRvAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardlibrary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibraryRvAdapter.ViewHolder, position: Int) {
        val data = listEgg[position]
        holder.apply {
            Glide.with(context)
                .load(data.imageUrl)
                .into(image)

            label.text = data.label
            date.text = data.date
            menu.setOnClickListener {

            }

        }

    }

    override fun getItemCount(): Int {
        return listEgg.size
    }


}