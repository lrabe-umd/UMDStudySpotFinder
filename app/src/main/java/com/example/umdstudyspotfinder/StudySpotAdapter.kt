package com.example.umdstudyspotfinder

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

// This class essentially tells the RecyclerView how to update
// the study_spot_item.xml based on each StudySpot
class StudySpotAdapter(
    val spots: MutableList<StudySpot>,
): RecyclerView.Adapter<StudySpotAdapter.SpotViewHolder>() {

    var highlightedIndex: Int? = null

    inner class SpotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.spot_name)
        val tagText: TextView = itemView.findViewById(R.id.spot_tags)
        val infoButton: ImageButton = itemView.findViewById(R.id.infoButton)

        fun bind(spot: StudySpot) {
            nameText.text = spot.name

            var tagList: String = ""
            for(tag in spot.tags) {
                var formattedTag: String = ""
                formattedTag = when(tag) {
                    "group_study" -> "Group Study"
                    "reservable" -> "Reservable"
                    "outlets" -> "Outlets"
                    "whiteboard" -> "Whiteboard"
                    "windows" -> "Windows"
                    "quiet_study" -> "Quiet"
                    "outside" -> "Outdoors"
                    "food_nearby" -> "Food Nearby"
                    else -> "Unknown Tag"
                }

                tagList += formattedTag
                tagList += ", "
            }
            tagList = tagList.dropLast(2) // remove last 2 chars (, )
            tagText.text = tagList


            infoButton.setOnClickListener {
                val intent = Intent(itemView.context, MoreInfoActivity::class.java)
                intent.putExtra("spot_id", spot.id)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.study_spot_item, parent, false)
        return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        if(highlightedIndex == position) {
            holder.itemView.findViewById<RelativeLayout>(R.id.spot_details).setBackgroundColor(Color.LTGRAY)
        } else {
            holder.itemView.findViewById<RelativeLayout>(R.id.spot_details).setBackgroundColor(Color.WHITE)
        }

        holder.bind(spots[position])
    }

    override fun getItemCount(): Int {
        return spots.size
    }
}