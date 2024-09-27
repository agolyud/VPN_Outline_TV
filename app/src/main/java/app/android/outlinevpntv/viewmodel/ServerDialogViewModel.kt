package app.android.outlinevpntv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.android.outlinevpntv.data.remote.ParseUrlOutline

class ServerDialogViewModel(
    private val validateOutlineUrl: ParseUrlOutline.Validate
) : ViewModel() {

    fun validate(ssUrl: String): Boolean {
        return validateOutlineUrl.validate(ssUrl)
    }

    class Factory(
        private val validateOutlineUrl: ParseUrlOutline.Validate
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ServerDialogViewModel(validateOutlineUrl) as T
        }
    }
}