package com.example.taskmate.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.taskmate.R
import com.example.taskmate.home.fonts

@Composable
fun ProfileScreen() {
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        var name by remember { mutableStateOf("Harsh Suthar") }
        var email by remember { mutableStateOf("harshsuthar9799@gmail.com") }
        var phoneNo by remember { mutableStateOf("2222444488") }
        var birthDate by remember { mutableStateOf("May 14, 2005") }
        var bio by remember { mutableStateOf("") }

        val(text1,text2,text3,profileImage,box,box1,box2,box3,box4,box5,saveButton) = createRefs()

        Text("Edit Profile", modifier = Modifier.constrainAs(text1) {
            top.linkTo(parent.top, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 20.sp, fontFamily = fonts, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
            color = Color(0xFF24252C)
        )

        Image(painter = painterResource(R.drawable.picofme), contentDescription = "profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.constrainAs(profileImage) {
                top.linkTo(text1.bottom, margin = 35.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.size(122.dp).clip(CircleShape)
        )

        Box(modifier = Modifier.constrainAs(box) {
            top.linkTo(profileImage.bottom, margin = (-22).dp)
            start.linkTo(profileImage.start)
            end.linkTo(profileImage.end)
        }.shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(15.dp),
            ambientColor = Color(0xFF5F33E1).copy(alpha = 0.2f),
            spotColor = Color(0xFF5F33E1).copy(alpha = 0.4f)
        ).background(Color(0xFF5F33E1),
            shape = RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Change", modifier = Modifier.padding(top = 5.dp, bottom = 5.dp, start = 10.dp, end = 10.dp),
                fontSize = 12.sp, lineHeight = 17.sp, fontFamily = fonts,
                fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal,
                color = Color(0xFFEDE8FF)
            )
        }

        Text("Harsh Suthar", modifier = Modifier.constrainAs(text2) {
            top.linkTo(box.bottom, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 22.sp, lineHeight = 25.sp, fontFamily = fonts, fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal, color = Color(0xFF24252C)
        )

        Text("21 Years Old", modifier = Modifier.constrainAs(text3) {
            top.linkTo(text2.bottom, margin = 5.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }, fontSize = 14.sp, lineHeight = 17.sp, fontFamily = fonts, fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal, color = Color(0xFF6E6A7C)
        )

        Box(modifier = Modifier.constrainAs(box1) {
            top.linkTo(text3.bottom, margin = 25.dp)
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
                val(boxShape,text1,text2) = createRefs()

                Box(modifier = Modifier.constrainAs(boxShape) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 15.dp)
                }.size(34.dp).background(Color(0xFFEDE8FF),
                    shape = RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                )  {
                    Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.task_icon),
                        contentDescription = "User Icon", tint = Color(0xFF5F33E1)
                    )
                }

                Text("Full Name", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 13.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                )

                if (name.isEmpty()) {
                    Text(modifier = Modifier.constrainAs(text3) {
                        top.linkTo(text1.bottom, margin = 8.dp)
                        start.linkTo(parent.start, margin = 15.dp) },
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
                            .constrainAs(text2) {
                                top.linkTo(text1.bottom, margin = 8.dp)
                                start.linkTo(boxShape.end, margin = 10.dp)
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

        Box(modifier = Modifier.constrainAs(box2) {
            top.linkTo(box1.bottom, margin = 15.dp)
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
                val(boxShape,text1,text2) = createRefs()

                Box(modifier = Modifier.constrainAs(boxShape) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 15.dp)
                }.size(34.dp).background(Color(0xFFEDE8FF),
                    shape = RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                )  {
                    Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.task_icon),
                        contentDescription = "User Icon", tint = Color(0xFF5F33E1)
                    )
                }

                Text("Email Address", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 13.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                )

                if (email.isEmpty()) {
                    Text(modifier = Modifier.constrainAs(text3) {
                        top.linkTo(text1.bottom, margin = 8.dp)
                        start.linkTo(parent.start, margin = 15.dp) },
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
                            .constrainAs(text2) {
                                top.linkTo(text1.bottom, margin = 8.dp)
                                start.linkTo(boxShape.end, margin = 10.dp)
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

        Box(modifier = Modifier.constrainAs(box3) {
            top.linkTo(box2.bottom, margin = 15.dp)
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
                val(boxShape,text1,text2) = createRefs()

                Box(modifier = Modifier.constrainAs(boxShape) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 15.dp)
                }.size(34.dp).background(Color(0xFFEDE8FF),
                    shape = RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                )  {
                    Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.task_icon),
                        contentDescription = "User Icon", tint = Color(0xFF5F33E1)
                    )
                }

                Text("Phone Number", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 13.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                )

                if (phoneNo.isEmpty()) {
                    Text(modifier = Modifier.constrainAs(text3) {
                        top.linkTo(text1.bottom, margin = 8.dp)
                        start.linkTo(parent.start, margin = 15.dp) },
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
                            .constrainAs(text2) {
                                top.linkTo(text1.bottom, margin = 8.dp)
                                start.linkTo(boxShape.end, margin = 10.dp)
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

        Box(modifier = Modifier.constrainAs(box4) {
            top.linkTo(box3.bottom, margin = 15.dp)
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
                val(boxShape,text1,text2,calendar) = createRefs()

                Box(modifier = Modifier.constrainAs(boxShape) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 15.dp)
                }.size(34.dp).background(Color(0xFFEDE8FF),
                    shape = RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                )  {
                    Icon(modifier = Modifier.size(20.dp), painter = painterResource(R.drawable.task_icon),
                        contentDescription = "User Icon", tint = Color(0xFF5F33E1)
                    )
                }

                Text("Birth Date", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 13.dp)
                    start.linkTo(boxShape.end, margin = 10.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                )

                if (birthDate.isEmpty()) {
                    Text(modifier = Modifier.constrainAs(text3) {
                        top.linkTo(text1.bottom, margin = 8.dp)
                        start.linkTo(parent.start, margin = 15.dp) },
                        text = "Enter Birth Date",
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
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        modifier = Modifier
                            .constrainAs(text2) {
                                top.linkTo(text1.bottom, margin = 8.dp)
                                start.linkTo(boxShape.end, margin = 10.dp)
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

                Icon(painterResource(R.drawable.calendar), contentDescription = "calendar Icon",
                    modifier = Modifier.constrainAs(calendar) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 15.dp)
                    }, tint = Color(0xFF5F33E1)
                )
            }
        }

        Box(modifier = Modifier.constrainAs(box5) {
            top.linkTo(box4.bottom, margin = 15.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
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
                val(text1,text2,text3) = createRefs()

                Text("Bio (Optional)", modifier = Modifier.constrainAs(text1) {
                    top.linkTo(parent.top, margin = 12.dp)
                    start.linkTo(parent.start, margin = 15.dp)
                }, fontFamily = fonts, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Normal,
                    fontSize = 9.sp, lineHeight = 12.sp, color = Color(0xFF6E6A7C)
                )

                if (bio.isEmpty()) {
                    Text(modifier = Modifier.constrainAs(text3) {
                        top.linkTo(text1.bottom, margin = 8.dp)
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
                            .constrainAs(text2) {
                                top.linkTo(text1.bottom)
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

        Button(modifier = Modifier.constrainAs(saveButton) {
            start.linkTo(parent.start)
            bottom.linkTo(parent.bottom, margin = 15.dp)
        }.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
            onClick = {

            }, colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5F33E1),
                contentColor = Color(0xFFFFFFFF)
            ) , shape = RoundedCornerShape(10.dp)) {

            Text("Saved Change", fontFamily = fonts, fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal, fontSize = 18.sp
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ShowProfileScreen() {
    ProfileScreen()
}