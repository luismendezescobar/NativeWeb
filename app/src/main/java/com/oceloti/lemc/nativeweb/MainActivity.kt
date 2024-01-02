package com.oceloti.lemc.nativeweb

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.oceloti.lemc.nativeweb.ui.theme.NativeWebTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      NativeWebTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MyApp()
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MyApp() {
  var searchText by remember { mutableStateOf("") }
  var showWebView by remember { mutableStateOf(true) }
  var savedSearches by remember { mutableStateOf(emptyList<String>()) }
  val LightBlueColor = Color(0xFFADD8E6) // Light Blue
  val LightRedColor = Color(0xFFFFB6C1) // Light Red
  val LightGreenColor = Color(0xFF90EE90) // Light Green


  val context = LocalContext.current
  val colorSearch: Color
  val keyboardController = LocalSoftwareKeyboardController.current

  // LaunchedEffect to load initial values when the composable is first launched
  LaunchedEffect(context) {
    savedSearches = getSavedSearchesFromSharedPreferences(context)
  }


  Column {
    // App Bar
    TopAppBar(
      title = {
        TextField(
          value = searchText,
          onValueChange = { searchText = it },
          modifier = Modifier
            .fillMaxWidth()
            .background(if (isValidUrl(searchText)) LightBlueColor else LightRedColor),
          label = { Text("Search") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {keyboardController?.hide()})
        )
      },
      actions = {
        // Delete Icon
        IconButton(onClick = {
          searchText = ""
        }) {
          Icon(
            Icons.Filled.Delete,
            contentDescription = null,
            tint = Color.Red
          )
        }
        // Toggle Button
        IconButton(onClick = {
          showWebView = !showWebView
        }) {
          if (showWebView) {
            Icon(
              Icons.Filled.Checklist,
              contentDescription = null,
              tint = Color.Blue
            )
          } else {
            Icon(
              Icons.Filled.Web,
              contentDescription = null,
              tint = Color.Magenta
            )
          }
        }
        // Star Icon
        IconButton(onClick = {
          if(!searchText.isEmpty()){
            saveSearchToSharedPreferences(context, searchText)

            // Refresh the list of saved searches
            savedSearches = getSavedSearchesFromSharedPreferences(context)
          }
        }) {
          Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = Color.Yellow
          )
        }

      }
    )

    // Web View or List based on the state
    if (showWebView) {
      // WebView
      AndroidView(
        factory = { context ->
          WebView(context).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.domStorageEnabled = true
          }
        },
        update = { webView ->
          // Load the URL from the search bar
          if(isValidUrl(searchText)){
            if(searchText.equals("https") || searchText.equals("https:") || searchText.equals("www") || searchText.equals("www.") ){
              webView.loadUrl("https://www.google.com/search?q=$searchText")
            }else{
              webView.loadUrl(searchText)
            }
          }else{
            webView.loadUrl("https://www.google.com/search?q=$searchText")
          }
        },
        modifier = Modifier
          .fillMaxSize()
          .background(Color.White)
      )
    } else {
      // List of saved searches
      LazyColumn {
        if (savedSearches.isEmpty()) {
          item {
            Text(
              text = "Go search something interesting!",
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            )
          }
        } else {
          items(savedSearches) { savedSearch ->
            SelectionContainer {
              Text(
                text = savedSearch,
                modifier = Modifier.padding(16.dp)
              )
            }
          }
          // Delete All Icon Button
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalArrangement = Arrangement.End
            ) {
              IconButton(onClick = {
                // Delete all saved searches
                clearSharedPreferences(context)
                // Refresh the list of saved searches
                savedSearches = getSavedSearchesFromSharedPreferences(context)
              }) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = null,
                  tint = Color.Gray
                )
              }
            }
          }


        }
      }
    }

  }
}

// Extracted function to save search to SharedPreferences
@SuppressLint("MutatingSharedPrefs")
private fun saveSearchToSharedPreferences(context: Context, searchText: String) {
  val sharedPreferences = context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
  val editor = sharedPreferences.edit()

  // Retrieve the existing list of searches
  val savedSearchesSet = sharedPreferences.getStringSet("savedSearches", mutableSetOf()) ?: mutableSetOf()

  // Add the new search text to the list
  savedSearchesSet.add(searchText)

  // Save the updated list back to SharedPreferences
  editor.putStringSet("savedSearches", savedSearchesSet)
  editor.apply()
}

// Extracted function to get the latest saved searches from SharedPreferences
private fun getSavedSearchesFromSharedPreferences(context: Context): List<String> {
  val sharedPreferences = context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
  val savedSearchesSet = sharedPreferences.getStringSet("savedSearches", mutableSetOf()) ?: mutableSetOf()
  return savedSearchesSet.toList()
}

fun clearSharedPreferences(context: Context) {
  val sharedPreferences = context.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
  val editor = sharedPreferences.edit()
  editor.clear()
  editor.apply()
}

fun isValidUrl(url: String): Boolean {
  val patterns = listOf(
    "^https?://.*".toRegex(RegexOption.IGNORE_CASE),
    "^www\\..*".toRegex(RegexOption.IGNORE_CASE)
    // Add more patterns if needed
  )

  return patterns.any { it.matches(url) }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  NativeWebTheme {
    MyApp()
  }
}