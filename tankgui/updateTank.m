function updateTank(hObject, eventdata, handles)
% Parse content of Tank and update everything

p = get(hObject, 'Parent');

% load "globals"
topology = get(findobj('Tag','topology','Parent',p),'UserData');
dim = get(findobj('Tag','dim','Parent',p),'UserData');
h_tanks = get(findobj('Tag','h_tanks','Parent',p),'UserData');
h_inter = get(findobj('Tag','h_inter','Parent',p),'UserData');

% get new matrix
str = get(hObject, 'String');
new_m = txt_to_m(str, dim);
if isnan(new_m),
    disp('Error!');
    return;
end;
new_str = m_to_txt(new_m, dim);

cur_tank = get(hObject, 'UserData');

% update all tanks of same number
c = h_tanks;
for i = 1 : numel(c)
    if c(i)~=0 && get(c(i),'UserData') == cur_tank
        set(c(i), 'String', new_str);
    end;
end;
    
% update intermediate results
isfirst = 1;
upper = 1;
factor = zeros(dim,dim);
upperproduct = zeros(dim,dim);
for i = 1:numel(topology)
    cur = topology(i);
    
    if ~isnan(str2double(cur)) && isfirst
        % have tank, first in chain
        cur = str2double(cur);
        factor = txt_to_m(get(h_tanks(i),'String'),dim);
        isfirst = 0;
    elseif ~isnan(str2double(cur)) && ~isfirst
        % have tank, next in chain
        cur = str2double(cur);
        product = factor * txt_to_m(get(h_tanks(i),'String'),dim);
        set(h_inter(i),'String',m_to_txt(product, dim));
        factor = product;
    elseif cur == ' '
        % nothing
    elseif cur == '-'
        % switch to lower branch
        upper = 0;
        upperproduct = product;
        isfirst = 1;
    elseif cur == ';'
        % display chamber result
        set(h_inter(i),'String',m_to_txt(upperproduct - product, dim));
        % todo reset for next engine
    else
        error('Error parsing topology!');
    end;
end; 
       
end