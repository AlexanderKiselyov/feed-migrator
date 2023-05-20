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

if (code === null || code.length === 0) {
    copyButton.style.visibility = "hidden";
    codeField.style.visibility = "hidden";
    const main = document.getElementsByClassName("main")[0];

    const element = document.createElement("a");
    element.classList.add("main-bot_redirect");
    element.href = "https://t.me/FeedMigratorBot";
    element.innerHTML =
        `<button class="main-bot_redirect-button">Feed Migrator Bot</button>`;
    main.appendChild(element);
}

function copyToClipboard(text) {
    if (window.clipboardData && window.clipboardData.setData) {
        // IE specific code path to prevent textarea being shown while dialog is visible.
        return clipboardData.setData('Text', text);

    }
    if (document.queryCommandSupported && document.queryCommandSupported('copy')) {
        const textarea = document.createElement('textarea');
        textarea.textContent = text;
        textarea.style.position = 'fixed'; // Prevent scrolling to bottom of page in MS Edge.
        document.body.append(textarea);
        textarea.select();
        try {
            return document.execCommand('copy'); // Security exception may be thrown by some browsers.
        } catch (error) {
            console.warn('Copy to clipboard failed.', error);
            return false;
        } finally {
            document.body.removeChild(textarea);
        }
    }

}

copyButton.addEventListener("click", () => {
    if (code !== null && code.length !== 0) {
        copyToClipboard(code);
        alert("Код был успешно скопирован!");
    } else {
        console.error('Code is empty and cannot be copied.');
        alert("Что-то пошло не так, пожалуйста, попробуйте вернуться и открыть страницу заново.");
    }
})

