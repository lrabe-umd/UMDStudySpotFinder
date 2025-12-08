package com.example.umdstudyspotfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// This class essentially tells the RecyclerView how to update
// the study_spot_item.xml based on each StudySpot
class StudySpotAdapter(
    private val spots: MutableList<StudySpot>,
): RecyclerView.Adapter<StudySpotAdapter.SpotViewHolder>() {

    inner class SpotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.spot_name)
        val tagText: TextView = itemView.findViewById(R.id.spot_tags)
        val favButton: ImageButton = itemView.findViewById(R.id.favButton)
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

            // TODO: Dynamic heart button
            /*
            if(spotFavorited) {
                show filled heart
            } else {
                show empty heart
            }
             */

            // TODO: Listeners
            favButton.setOnClickListener {
                // Favorite the spot and update image
            }

            infoButton.setOnClickListener {
                // Start the spot info activity
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.study_spot_item, parent, false)
        return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        holder.bind(spots[position])
    }

    override fun getItemCount(): Int {
        return spots.size
    }
}