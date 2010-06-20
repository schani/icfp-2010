function build_gui (topology, dim)

firstx = 3;
firsty = 1;
sizex = 4*dim+6;
sizey = dim+1;
labelsizey = 1;
spacex = firstx;
spacey = firsty;
uoffy = 2*sizey+2*spacey+labelsizey;


f = figure('Position',[10,10,1024,768]);

x = firstx;
y = firsty;
isfirst = 1;
upper = 1;
for i = 1:numel(topology)
    cur = topology(i);
    
    if ~isnan(str2double(cur)) && isfirst
        % have tank, first in chain
        cur = str2double(cur);
        h_tanks(i) = uicontrol('Style','edit','String',m_to_txt(zeros(dim,dim),dim), 'UserData', cur,...
            'Units', 'characters', 'Position', [x,y+upper*uoffy,sizex,sizey], 'Max', 3.0, ...
            'Callback', 'updateTank(gcbo,[],guidata(gcbo))');
        uicontrol('Style','text','String',sprintf('%d', cur), ...
            'Units', 'characters', 'Position', [x,y+sizey+upper*uoffy,sizex,labelsizey]);
        isfirst = 0;
    elseif ~isnan(str2double(cur)) && ~isfirst
        % have tank, next in chain
        cur = str2double(cur);
        h_tanks(i) = uicontrol('Style','edit','String',m_to_txt(zeros(dim,dim),dim), 'UserData', cur,...
            'Units', 'characters', 'Position', [x,y+sizey+spacey+upper*uoffy,sizex,sizey], 'Max', 3.0, ...
            'Callback', 'updateTank(gcbo,[],guidata(gcbo))');
        uicontrol('Style','text','String',sprintf('%d', cur), ...
            'Units', 'characters', 'Position', [x,y+2*sizey+spacey+upper*uoffy,sizex,labelsizey]);
        h_inter(i) = uicontrol('Style','text','String',m_to_txt(zeros(dim,dim),dim), ...
            'Units', 'characters', 'Position', [x,y+upper*uoffy,sizex,sizey], 'Max', 3.0);
    elseif cur == ' '
        % nothing
    elseif cur == '-'
        if isfirst, error('No upper branch!'); end;
        % switch to lower branch
        upper = 0;
        isfirst = 1;
        x = firstx - sizex - spacex;
    elseif cur == ';'
        if isfirst || upper, error('No lower branch!'); end;
        % display result box
        h_inter(i) = uicontrol('Style','text','String',m_to_txt(zeros(dim,dim),dim), ...
            'Units', 'characters', 'Position', [x,y+uoffy/2,sizex,sizey], 'Max', 3.0, ...
            'BackgroundColor', 'y');
        % todo reset for next engine
    else
        error('Error parsing topology!');
    end;
    
    x = x + sizex + spacex;   
end; 


% add a couple of hidden labels to fake globals
uicontrol('Style', 'text', 'Visible', 'off', 'Tag', 'topology', 'UserData', topology);
uicontrol('Style', 'text', 'Visible', 'off', 'Tag', 'dim', 'UserData', dim);
uicontrol('Style', 'text', 'Visible', 'off', 'Tag', 'h_tanks', 'UserData', h_tanks);
uicontrol('Style', 'text', 'Visible', 'off', 'Tag', 'h_inter', 'UserData', h_inter);

end


    








%htext = uicontrol('Style','edit','String',sprintf('  1   0   0\n  0   0   0\n  0   0   0'), 'Units', 'characters', 'Position', [3,1,18,4], 'Max', 3.0)
