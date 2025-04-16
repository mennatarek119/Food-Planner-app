import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.R
import com.example.hmnm.database.entities.FavoriteMeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesFragment : Fragment() {

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoriteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView)
        favoritesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        favoritesAdapter = FavoriteAdapter(mutableListOf()) { meal ->
            deleteFavoriteFromFirestore(meal)
        }
        favoritesRecyclerView.adapter = favoritesAdapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            loadFavoritesFromFirestore(userId)
        } else {
            showErrorMessage("User not logged in")
        }
    }

    private fun loadFavoritesFromFirestore(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val favoritesRef = db.collection("favorites")
            .document(userId)
            .collection("meals")

        favoritesRef.get()
            .addOnSuccessListener { documents ->
                val favoriteList = mutableListOf<FavoriteMeal>()
                for (document in documents) {
                    val favorite = document.toObject(FavoriteMeal::class.java)
                    Log.d("Favorites", "Loaded: ${favorite.strMeal} - ${favorite.idMeal}")
                    favoriteList.add(favorite)
                }
                favoritesAdapter.updateItems(favoriteList)
            }
            .addOnFailureListener { e ->
                Log.e("Favorites", "Error fetching favorites", e)
                showErrorMessage("فشل تحميل المفضلات من Firestore")
            }
    }


    private fun deleteFavoriteFromFirestore(favoriteMeal: FavoriteMeal) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("favorites")
            .document(userId)
            .collection("meals")
            .document(favoriteMeal.idMeal)
            .delete()
            .addOnSuccessListener {
                Log.d("Favorites", "Deleted: ${favoriteMeal.idMeal}")
                loadFavoritesFromFirestore(userId)
            }
            .addOnFailureListener {
                showErrorMessage("فشل حذف الوجبة من Firestore")
            }
    }


    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        favoritesRecyclerView.visibility = View.GONE
    }
}
