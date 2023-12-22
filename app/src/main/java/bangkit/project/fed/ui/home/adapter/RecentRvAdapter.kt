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
import com.bumptech.glide.Glide

class RecentRvAdapter(private val context: Context) :
    RecyclerView.Adapter<RecentRvAdapter.ViewHolder>() {
    private val listEgg = mutableListOf<EggData>()
    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image : ImageView = itemView.findViewById(R.id.recentImage)
        val name : TextView = itemView.findViewById(R.id.recentName)
        val date : TextView = itemView.findViewById(R.id.recentDate)


    }

    fun submitList(newList: List<EggData>) {
        listEgg.clear()
        listEgg.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardrecent, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listEgg.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listEgg[position]
        holder.apply {
            Glide.with(context)
                .load(data.imageUrl)
                .into(image)

            name.text = data.label
            date.text = data.date
        }
    }
}