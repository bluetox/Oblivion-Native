package com.example.oblivion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.compose.material3.Button
import com.example.oblivion.RustBridge.createProfile


class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int) :
    RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount
        outRect.top = spacing
        outRect.bottom = spacing
    }
}
class ProfilesAdapter(
    private val profiles: List<Profile>,
    private val onProfileClick: () -> Unit
) : RecyclerView.Adapter<ProfilesAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.profileName)
        val image: ImageView = view.findViewById(R.id.profileImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]
        holder.name.text = profile.user_id
        holder.image.setImageResource(R.drawable.avatar_background)

        holder.itemView.setOnClickListener {
            val res = RustBridge.loadWithProfile(profile.user_id, "e")
            Log.d("RUST", "Profile loaded status: $res")
            onProfileClick()
        }
    }

    override fun getItemCount(): Int = profiles.size
}

class ProfileMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noProfilesText = view.findViewById<TextView>(R.id.tvEmpty)
        val recycler = view.findViewById<RecyclerView>(R.id.rvProfiles)
        val btnCreate = view.findViewById<Button>(R.id.btnCreate)

        val json: String = RustBridge.getProfiles()

        val type = object : com.google.gson.reflect.TypeToken<List<Profile>>() {}.type
        val profiles: List<Profile> = com.google.gson.Gson().fromJson(json, type)

        if (profiles.isEmpty()) {
            noProfilesText.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            noProfilesText.visibility = View.GONE
            recycler.visibility = View.VISIBLE

            val displayMetrics = resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels / displayMetrics.density
            val scalingFactor = 160f
            val spanCount = (dpWidth / scalingFactor).toInt().coerceAtLeast(1)

            recycler.layoutManager = GridLayoutManager(requireContext(), spanCount)
            recycler.adapter = ProfilesAdapter(profiles) {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_left, R.anim.slide_out_right,
                        R.anim.slide_in_right, R.anim.slide_out_left
                    )
                    .replace(R.id.fragment_container, HomeFragment())
                    .addToBackStack(null)
                    .commit()
            }

            val spacingInPixels = (16 * displayMetrics.density).toInt()
            recycler.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingInPixels))
        }

        btnCreate.setOnClickListener {
            createProfile("e", "bluetox")
        }
    }


}
