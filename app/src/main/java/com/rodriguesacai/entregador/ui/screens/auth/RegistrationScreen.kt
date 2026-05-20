package com.rodriguesacai.entregador.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodriguesacai.entregador.ui.components.FormField
import com.rodriguesacai.entregador.ui.components.PrimaryAction
import com.rodriguesacai.entregador.ui.components.SecondaryAction
import com.rodriguesacai.entregador.ui.components.Stepper
import com.rodriguesacai.entregador.ui.components.TopBar
import com.rodriguesacai.entregador.ui.components.UpCard
import com.rodriguesacai.entregador.ui.components.UploadBox
import com.rodriguesacai.entregador.ui.design.UpColors

@Composable
fun RegistrationScreen(
    onBack: () -> Unit,
    onSubmit: (String, String, String, String, String?, String?) -> Unit,
    onFinish: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var veiculo by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf<Uri?>(null) }
    var selfie by remember { mutableStateOf<Uri?>(null) }
    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { documento = it }
    val selfiePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selfie = it }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TopBar("Solicitar cadastro", onBack)
        Stepper(1, listOf("Dados", "Documentos", "Confirmação", "Conclusão"))
        UpCard {
            Text("Dados pessoais", color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 17.sp)
            FormField(nome, { nome = it }, "Nome completo", Icons.Rounded.Person)
            FormField(cpf, { cpf = it.filter(Char::isDigit).take(11) }, "CPF", Icons.Rounded.Person, KeyboardType.Number)
            FormField(telefone, { telefone = it.filter(Char::isDigit).take(11) }, "Telefone", Icons.Rounded.Phone, KeyboardType.Phone)
            FormField(email, { email = it }, "E-mail", Icons.Rounded.Email, KeyboardType.Email)
            FormField(cidade, { cidade = it }, "Cidade", Icons.Rounded.LocationOn)
            FormField(veiculo, { veiculo = it }, "Veículo", Icons.Rounded.DirectionsBike)
            FormField(placa, { placa = it.uppercase().take(8) }, "Placa", Icons.Rounded.DirectionsBike)
        }
        UpCard {
            Text("Documentos", color = UpColors.Ink, fontWeight = FontWeight.Black, fontSize = 17.sp)
            UploadBox(if (documento == null) "Foto da CNH ou documento" else "Documento selecionado", "PNG, JPG ou PDF até 5MB", onClick = { docPicker.launch("*/*") })
            UploadBox(if (selfie == null) "Selfie do condutor" else "Selfie selecionada", "Imagem nítida do rosto", onClick = { selfiePicker.launch("image/*") })
        }
        PrimaryAction("Enviar cadastro", onClick = {
            onSubmit(nome, cpf, telefone, placa, documento?.toString(), selfie?.toString())
            onFinish()
        })
        SecondaryAction("Já tenho conta", onBack)
        Spacer(Modifier.height(20.dp))
    }
}
