const params = new Proxy(new URLSearchParams(window.location.search), {
    get: (searchParams, prop) => searchParams.get(prop)
})

const custom_args = new Proxy(new URLSearchParams(params.custom_args), {
    get: (searchParams, prop) => searchParams.get(prop)
})

const code = custom_args.code;

const codeField = document.getElementsByClassName("main-code")[0];
codeField.textContent = code

const copyButton = document.getElementsByClassName("main-copyButton")[0];

function copyToClipboard() {
    const text = codeField.innerText;
    const textarea = document.createElement("textarea");
    document.body.appendChild(textarea);
    textarea.value = text;
    textarea.select();
    let isDone;
    try {
        navigator.clipboard.writeText(textarea.value);
        isDone = true;
    } catch (error) {
        console.warn('Copy to clipboard failed.', error);
        isDone = false;
    } finally {
        document.body.removeChild(textarea);
    }

    return isDone;
}

copyButton.addEventListener("click", () => {
    if (code !== null && code.length !== 0 && copyToClipboard()) {
        alert("Код был успешно скопирован!");
    } else {
        console.error('Code is empty and cannot be copied.');
        alert("Что-то пошло не так, пожалуйста, попробуйте вернуться и открыть страницу заново.");
    }
})

