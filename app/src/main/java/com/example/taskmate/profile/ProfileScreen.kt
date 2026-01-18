package com.example.taskmate.profile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.edit
import coil.compose.AsyncImage
import com.example.taskmate.R
import com.example.taskmate.home.fonts
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.yalantis.ucrop.UCrop

object UserPrefs {
    private const val PREF_NAME = "task_prefs"
    private const val KEY_USER_PROFILE = "user_profile"

    private val gson = Gson()

    fun saveUser(context: Context, user: UserProfile) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_USER_PROFILE, gson.toJson(user))
        }
    }

    fun getUser(context: Context): UserProfile? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_USER_PROFILE, null) ?: return null
        return gson.fromJson(json, UserProfile::class.java)
    }

    fun clearUser(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_USER_PROFILE)
        }
    }

    fun updateProfileImage(context: Context, imageUri: String) {
        val user = getUser(context) ?: return
        saveUser(context, user.copy(profileImageUri = imageUri))
    }
}

@Composable
fun ProfileScreen(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedUser = remember { UserPrefs.getUser(context) }

    var name by remember { mutableStateOf(savedUser?.name ?: "") }
    var email by remember { mutableStateOf(savedUser?.email ?: "") }
    var phoneNo by remember { mutableStateOf(savedUser?.phone ?: "") }
    var bio by remember { mutableStateOf(savedUser?.bio ?: "") }

    var birthDate by remember {
        mutableStateOf(
            savedUser?.birthDate?.takeIf { it.isNotEmpty() }?.let {
                LocalDate.parse(it)
            }
        )
    }
    var profileImageUri by remember {
        mutableStateOf(savedUser?.profileImageUri ?: "")
    }

    val cropLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let {
                    profileImageUri = it.toString()
                    UserPrefs.updateProfileImage(context, it.toString())
                }
            }
        }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { sourceUri ->

                val destinationUri = Uri.fromFile(
                    File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg")
                )

                val options = UCrop.Options().apply {
                    setCircleDimmedLayer(true)
                    setShowCropFrame(false)
                    setShowCropGrid(false)
                }

                val intent = UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(512, 512)
                    .withOptions(options)
                    .getIntent(context.findActivity())

                cropLauncher.launch(intent)
            }
        }

    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (titleText,profileImage,changePhotoButton,userNameText,
            userAgeText,formContainer,saveButton) = createRefs()

        Text("Edit Profile", modifier = Modifier.constrainAs(titleText
        ) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        AsyncImage(
            model = profileImageUri.ifEmpty { R.drawable.picofme },
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.constrainAs(profileImage) {
                top.linkTo(titleText.bottom, margin = 30.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.size(128.dp).clip(CircleShape),
            placeholder = painterResource(R.drawable.picofme),
            error = painterResource(R.drawable.picofme)
        )

        Box(modifier = Modifier.constrainAs(changePhotoButton) {
            top.linkTo(profileImage.bottom, margin = (-24).dp)
            start.linkTo(profileImage.start)
            end.linkTo(profileImage.end)
        }.clickable {
            imagePickerLauncher.launch("image/*")
        }.shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(15.dp),
            ambientColor = Color(0xFF5F33E1).copy(alpha = 0.2f),
            spotColor = Color(0xFF5F33E1).copy(alpha = 0.4f)
        ).background(Color(0xFF5F33E1),
            shape = RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Change", modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 10.dp, end = 10.dp),
                fontSize = 12.sp, lineHeight = 17.sp, fontFamily = fonts,
                fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = Color(0xFFFFFFFF)
            )
        }

        Text("Harsh Suthar", modifier = Modifier.constrainAs(userNameText) {
            top.linkTo(changePhotoButton.bottom, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 22.sp, lineHeight = 25.sp, fontFamily = fonts, fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal, color = Color(0xFF24252C)
        )

        Text(text = birthDate?.let {
            "${calculateAge(it)} Years Old"
        } ?: "Years Old", modifier = Modifier.constrainAs(userAgeText) {
            top.linkTo(userNameText.bottom, margin = 5.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts, fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal, color = Color(0xFF6E6A7C)
        )

        Column(modifier = Modifier.constrainAs(formContainer) {
            top.linkTo(userAgeText.bottom, margin = 15.dp)
            bottom.linkTo(saveButton.top, margin = (-15).dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            height = Dimension.fillToConstraints
        }.verticalScroll(rememberScrollState())) {
            ConstraintLayout {
                val (fullNameField,emailField,phoneField,birthDateField,
                    bioField) = createRefs()

                Box(modifier = Modifier.constrainAs(fullNameField) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (iconBox, labelFullName, inputFullName, placeholderFullName) = createRefs()

                        Box(modifier = Modifier.constrainAs(iconBox) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 15.dp)
                        }.size(34.dp).background(Color(0xFFEDE8FF),
                            shape = RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        )  {
                            Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.user_edit),
                                contentDescription = "User Icon", tint = Color(0xFF5F33E1)
                            )
                        }

                        Text("Full Name", modifier = Modifier.constrainAs(labelFullName) {
                            top.linkTo(parent.top, margin = 13.dp)
                            start.linkTo(iconBox.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (name.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(placeholderFullName) {
                                top.linkTo(labelFullName.bottom, margin = 8.dp)
                                start.linkTo(iconBox.end, margin = 10.dp) },
                                text = "Enter Full Name",
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 14.sp, lineHeight = 17.sp,
                                color = Color(0xFF24252C)
                            )
                        }

                        val selectionColors = TextSelectionColors(
                            handleColor = Color(0xFFAB94FF),
                            backgroundColor = Color(0xFFAB94FF).copy(alpha = 0.3f)
                        )

                        CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                            BasicTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier
                                    .constrainAs(inputFullName) {
                                        top.linkTo(labelFullName.bottom, margin = 8.dp)
                                        start.linkTo(iconBox.end, margin = 10.dp)
                                        end.linkTo(parent.end, margin = 15.dp)
                                        width = Dimension.fillToConstraints
                                    },
                                textStyle = TextStyle(
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp,
                                    color = Color(0xFF24252C)
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF6A5AE0))
                            )
                        }
                    }
                }

                Box(modifier = Modifier.constrainAs(emailField) {
                    top.linkTo(fullNameField.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (iconBox, labelEmail, inputEmail, placeholderEmail) = createRefs()

                        Box(modifier = Modifier.constrainAs(iconBox) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 15.dp)
                        }.size(34.dp).background(Color(0xFFEDE8FF),
                            shape = RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        )  {
                            Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.user_email),
                                contentDescription = "User Email", tint = Color(0xFF5F33E1)
                            )
                        }

                        Text("Email Address", modifier = Modifier.constrainAs(labelEmail) {
                            top.linkTo(parent.top, margin = 13.dp)
                            start.linkTo(iconBox.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (email.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(placeholderEmail) {
                                top.linkTo(labelEmail.bottom, margin = 8.dp)
                                start.linkTo(iconBox.end, margin = 10.dp) },
                                text = "Enter Email Address",
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 14.sp, lineHeight = 17.sp,
                                color = Color(0xFF24252C)
                            )
                        }

                        val selectionColors = TextSelectionColors(
                            handleColor = Color(0xFFAB94FF),
                            backgroundColor = Color(0xFFAB94FF).copy(alpha = 0.3f)
                        )

                        CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                            BasicTextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier
                                    .constrainAs(inputEmail) {
                                        top.linkTo(labelEmail.bottom, margin = 8.dp)
                                        start.linkTo(iconBox.end, margin = 10.dp)
                                        end.linkTo(parent.end, margin = 15.dp)
                                        width = Dimension.fillToConstraints
                                    },
                                textStyle = TextStyle(
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp,
                                    color = Color(0xFF24252C)
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF6A5AE0))
                            )
                        }
                    }
                }

                Box(modifier = Modifier.constrainAs(phoneField) {
                    top.linkTo(emailField.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (iconBox, labelPhone, inputPhone, placeholderPhone) = createRefs()

                        Box(modifier = Modifier.constrainAs(iconBox) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 15.dp)
                        }.size(34.dp).background(Color(0xFFEDE8FF),
                            shape = RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        )  {
                            Icon(modifier = Modifier.size(18.dp), painter = painterResource(R.drawable.user_phone),
                                contentDescription = "User Phone", tint = Color(0xFF5F33E1)
                            )
                        }

                        Text("Phone Number", modifier = Modifier.constrainAs(labelPhone) {
                            top.linkTo(parent.top, margin = 13.dp)
                            start.linkTo(iconBox.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (phoneNo.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(placeholderPhone) {
                                top.linkTo(labelPhone.bottom, margin = 8.dp)
                                start.linkTo(iconBox.end, margin = 10.dp) },
                                text = "Enter Phone Number",
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 14.sp, lineHeight = 17.sp,
                                color = Color(0xFF24252C)
                            )
                        }

                        val selectionColors = TextSelectionColors(
                            handleColor = Color(0xFFAB94FF),
                            backgroundColor = Color(0xFFAB94FF).copy(alpha = 0.3f)
                        )

                        CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                            BasicTextField(
                                value = phoneNo,
                                onValueChange = { phoneNo = it },
                                modifier = Modifier
                                    .constrainAs(inputPhone) {
                                        top.linkTo(labelPhone.bottom, margin = 8.dp)
                                        start.linkTo(iconBox.end, margin = 10.dp)
                                        end.linkTo(parent.end, margin = 15.dp)
                                        width = Dimension.fillToConstraints
                                    },
                                textStyle = TextStyle(
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 14.sp, lineHeight = 17.sp,
                                    color = Color(0xFF24252C)
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF6A5AE0))
                            )
                        }
                    }
                }

                Box(modifier = Modifier.constrainAs(birthDateField) {
                    top.linkTo(phoneField.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }.padding(horizontal = 20.dp).height(63.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (iconBox, labelBirthDate, selectedBirthDate, calendarIcon) = createRefs()

                        Box(modifier = Modifier.constrainAs(iconBox) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 15.dp)
                        }.size(34.dp).background(Color(0xFFEDE8FF),
                            shape = RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        )  {
                            Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.cake_icon),
                                contentDescription = "Cake Icon", tint = Color(0xFF5F33E1)
                            )
                        }

                        Text("Birth Date", modifier = Modifier.constrainAs(labelBirthDate) {
                            top.linkTo(parent.top, margin = 13.dp)
                            start.linkTo(iconBox.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        Text(text = birthDate?.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy")
                        ) ?: "Select Birth Date", modifier = Modifier.constrainAs(selectedBirthDate) {
                            top.linkTo(labelBirthDate.bottom, margin = 7.dp)
                            start.linkTo(iconBox.end, margin = 10.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF24252C)
                        )

                        Icon(painterResource(R.drawable.calendar), contentDescription = "calendar Icon",
                            modifier = Modifier.constrainAs(calendarIcon) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                                end.linkTo(parent.end, margin = 15.dp)
                            }.clickable { showDatePicker = true}, tint = Color(0xFF5F33E1)

                        )
                    }
                }

                Box(modifier = Modifier.constrainAs(bioField) {
                    top.linkTo(birthDateField.bottom, margin = 15.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 25.dp)
                }.padding(horizontal = 20.dp).height(112.dp).fillMaxWidth().shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(15.dp),
                    ambientColor = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                    spotColor = Color(0xFFFFFFFF).copy(alpha = 0.4f)
                ).background(Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(15.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (labelBio, inputBio, placeholderBio) = createRefs()

                        Text("Bio (Optional)", modifier = Modifier.constrainAs(labelBio) {
                            top.linkTo(parent.top, margin = 12.dp)
                            start.linkTo(parent.start, margin = 15.dp)
                        }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                            fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                        )

                        if (bio.isEmpty()) {
                            Text(modifier = Modifier.constrainAs(placeholderBio) {
                                top.linkTo(labelBio.bottom, margin = 8.dp)
                                start.linkTo(parent.start, margin = 15.dp) },
                                text = "Write Your Bio Here...",
                                fontFamily = fonts,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontSize = 11.sp,
                                color = Color(0xFF24252C)
                            )
                        }

                        val selectionColors = TextSelectionColors(
                            handleColor = Color(0xFFAB94FF),
                            backgroundColor = Color(0xFFAB94FF).copy(alpha = 0.3f)
                        )

                        CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                            BasicTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                modifier = Modifier
                                    .constrainAs(inputBio) {
                                        top.linkTo(labelBio.bottom)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(parent.start)
                                    }
                                    .fillMaxWidth().fillMaxHeight()
                                    .padding(horizontal = 15.dp, vertical = 17.dp),
                                textStyle = TextStyle(
                                    fontFamily = fonts,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = 11.sp,
                                    color = Color(0xFF24252C)
                                ),
                                singleLine = false,
                                cursorBrush = SolidColor(Color(0xFF6A5AE0))
                            )
                        }
                    }
                }
            }
        }

        Button(modifier = Modifier.constrainAs(saveButton) {
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom, margin = 15.dp)
        }.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
            onClick = {
                val user = UserProfile(
                    name = name.trim(),
                    email = email.trim(),
                    phone = phoneNo.trim(),
                    birthDate = birthDate?.toString() ?: "",
                    bio = bio.trim(),
                    profileImageUri = ""
                )

                UserPrefs.saveUser(context, user)

                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Profile updated successfully",
                        duration = SnackbarDuration.Short
                    )
                }
            }, colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5F33E1),
                contentColor = Color(0xFFFFFFFF)
            ) , shape = RoundedCornerShape(10.dp)) {

            Text("Saved Change", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal, fontSize = 18.sp
            )
        }

        val customDatePickerColors = DatePickerDefaults.colors(
            containerColor = Color(0xFFEEE9FF),

            titleContentColor = Color(0xFF24252C),
            headlineContentColor = Color(0xFF5F33E1),
            navigationContentColor = Color(0xFFAB94FF),

            weekdayContentColor = Color(0xFF6E6A7C),

            dayContentColor = Color(0xFF24252C),
            disabledDayContentColor = Color(0xFF6E6A7C),

            selectedDayContainerColor = Color(0xFF5F33E1),
            selectedDayContentColor = Color(0xFFFFFFFF),

            todayContentColor = Color(0xFF5F33E1),
            todayDateBorderColor = Color(0xFFAB94FF),

            yearContentColor = Color(0xFF24252C),
            selectedYearContainerColor = Color(0xFF5F33E1),
            selectedYearContentColor = Color(0xFFFFFFFF)
        )
        val datePickerTypography = Typography(
            headlineSmall = TextStyle(
                fontSize = 20.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal
            ),
            headlineLarge = TextStyle(
                fontSize = 26.sp,
                fontFamily = fonts,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Normal
            ),
            titleLarge = TextStyle(
                fontFamily = fonts,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            ),
            titleMedium = TextStyle(
                fontFamily = fonts,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            ),
            labelLarge = TextStyle(
                fontFamily = fonts,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = fonts,
                fontWeight = FontWeight.Medium
            )
        )

        if (showDatePicker) {
            MaterialTheme(typography = datePickerTypography) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                birthDate = Instant.ofEpochMilli(selectedMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }) {
                            Text("OK", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = Color(0xFFAB94FF)
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal, color = Color(0xFFAB94FF)
                            )
                        }
                    }, colors = DatePickerDefaults.colors(
                        containerColor = Color(0xFFEEE9FF)
                    )
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = customDatePickerColors
                    )
                }
            }
        }
    }
}

private fun calculateAge(birthDate: LocalDate): Int {
    return Period.between(birthDate, LocalDate.now()).years
}

fun Context.findActivity(): Activity {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    error("Activity not found")
}

@Preview(showSystemUi = true)
@Composable
private fun ShowProfileScreen() {
    val snackbarHostState = SnackbarHostState()
    ProfileScreen(snackbarHostState)
}