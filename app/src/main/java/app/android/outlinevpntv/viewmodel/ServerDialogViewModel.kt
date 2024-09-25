package app.android.outlinevpntv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.android.outlinevpntv.data.remote.ParseUrlOutline

class ServerDialogViewModel(
    private val validateOutlineUrl: ParseUrlOutline.Validate
) : ViewModel() {

    fun validate(ssUrl: String): Boolean {
        return validateOutlineUrl.validate(ssUrl)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ServerDialogViewModel(validateOutlineUrl = ParseUrlOutline.Validate.Base())
            }
        }
    }
}