package app.codinguyy.coroutineexample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FirstFragmentViewModel() : ViewModel(){


    fun foo(){
        viewModelScope.launch{

        }
    }

}
